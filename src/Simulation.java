import java.math.BigInteger;
import java.util.Random;
import java.util.Scanner;
import java.util.Arrays;

import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.io.IOException;

public class Simulation extends Variables{

    //region MAIN
    public static void main(String[] args) {
        Simulation simulation = new Simulation();

        //region INGRESO
        Scanner in = new Scanner(System.in);
        System.out.println("Ingrese cantidad de puestos (threads): ");
        N = in.nextInt();
        System.out.println("Ingrese tamaño máximo de cola: ");
        MC = in.nextInt();
        System.out.println("Ingrese tiempo final (en ms): ");
        TF = in.nextBigInteger();
        //endregion

        simulation.initLogs();
        simulation.run();
        simulation.calculateResults();
        simulation.printResults();
        simulation.logResults();
    }
    //endregion

    //region LOGS
    public void initLogs() {
        try {
            String path = System.getProperty("user.dir");
            //Logs
            fhLogs = new FileHandler(path + "\\logs.log");
            logger = Logger.getLogger(path + "\\logs.log");
            logger.addHandler(fhLogs);
            //Results
            fhResults = new FileHandler(path + "\\results.log");
            logResults = Logger.getLogger(path + "\\results.log");
            logResults.addHandler(fhResults);

            fhLogs.setFormatter(new LogFormatter());
            fhResults.setFormatter(new LogFormatter());

            logger.info("initLogs successful");
            logResults.info("initLogs successful");
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logResults() {
        logResults.info("--------------RESULTS-----------------------");
        logResults.info("| Cantidad de puestos (threads): " + N + "|");
        logResults.info("| Tamaño máximo de cola: " + MC + "|");
        logResults.info("| Tiempo Final (en ms): " + TF + "|");
        logResults.info("| Cantidad de llegadas: " + NT + "|");
        logResults.info("| Promedio permanencia sistema: " + PPS + "|");
        logResults.info("| Promedio espera cola: " + PEC + "|");
        logResults.info("| Promedio tiempo atención: " + PTA + "|");
        logResults.info("| Porcentaje de rechazos: " + PR + "% |");
        logResults.info("--------------RESULTS-----------------------");
    }
    //endregion

    //region FUNCTIONS OPERATIONS
    public double functionIA(double x) {
        return (1 - Math.pow(1 - x, 0.834)) / Math.pow(1 - x, 0.834);
    }

    public double functionTA(double x) {
        return (LAMBDA_TA * K_TA * Math.pow(x / BETA_TA, LAMBDA_TA - 1)) / (BETA_TA * Math.pow(1 + Math.pow(x / BETA_TA, LAMBDA_TA), K_TA + 1));
    }
    //endregion

    //region GENERATORS OPERATIONS
    public void generateIA() {
        //MÉTODO DE LA FUNCIÓN INVERSA
        double r = new Random().nextDouble();
        IA = (int) functionIA(r);
        logger.info("IA generated = " + IA);
    }

    public void generateTA() {
        //MÉTODO DEL RECHAZO
        while (true) {
            double r1 = new Random().nextDouble();
            double r2 = new Random().nextDouble();

            double x1 = ((endTA - startTA) * r1 + startTA);
            double y1 = (maxTA * r2);

            if (functionTA(x1) >= y1) {
                TA = (int) x1;
                logger.info("TA generated = " + TA);
                return;
            }
        }
    }
    //endregion

    //region INITIALIZERS
    public void initConditions() {
        //Funciones
        startTA = 0;
        endTA = 999;
        maxTA = 1; //FIXME: No se si es 1

        //FIXME: El HV le debería asignar el tiempo final más el máximo tiempo de atención posible, pero podría ser más D:
        HV = BigInteger.valueOf(Long.MAX_VALUE);//TF.add(BigInteger.valueOf(endTA)); //TF + endTA

        T = BigInteger.valueOf(0);
        NS = 0;
        this.generateIA();
        TPLL = BigInteger.valueOf(IA);
        TPS = new BigInteger[N];
        Arrays.fill(TPS, HV); //Lleno todos los puestos de atención a libres

        //Resultados
        PPS = 0;
        PEC = 0;
        PTA = 0;

        NT = 0;
        STLL = BigInteger.valueOf(0); //0
        STS = BigInteger.valueOf(0); //0
        STA = BigInteger.valueOf(0); //0
    }
    //endregion

    //region THREADS OPERATIONS

    public int getMinThreadIndex() {
        BigInteger min = TPS[0];
        int index = 0;

        for (int i = 0; i < TPS.length; i++) {
            if (min.compareTo(TPS[i]) == 1) { // min > TPS[i]
                min = TPS[i];
                index = i;
            }
        }
        return index;
    }

    public int findFreeThreadIndex() {
        for (int i = 0; i < TPS.length; i++) {
            if (TPS[i] == HV)
                return i;
        }
        return -1;
    }

    //endregion

    //region RESULTS OPERATIONS
    public void calculateResults() {
        PPS = ((STS.subtract(STLL)).divide(BigInteger.valueOf(NT))).intValue(); //( STS - STLL) / NT;
        PEC = ((STS.subtract(STLL)).subtract(STA).divide(BigInteger.valueOf(NT))).intValue(); //(STS - STLL - STA) / NT;
        PTA = (STA.divide(BigInteger.valueOf(NT))).intValue(); //STA / NT;
        PR = (ARR * 100 / NT);
    }

    public void printResults() {
        System.out.println("Cantidad de puestos (threads): " + N);
        System.out.println("Tamaño máximo de cola: " + MC);
        System.out.println("Tiempo Final (en ms): " + TF);
        System.out.println("Cantidad de llegadas: " + NT);
        System.out.println("Promedio permanencia sistema: " + PPS);
        System.out.println("Promedio espera cola: " + PEC);
        System.out.println("Promedio tiempo atención: " + PTA);
        System.out.println("Porcentaje de rechazos: " + PR + "%");
    }
    //endregion OPERA

    //region RUNNER
    public void run() {
        this.initConditions();

        while ((T.compareTo(TF) < 0) || (NS != 0)) { //T<TF
            logger.info("--------------START-----------------------");
            logger.info("START: T = " + T + ", NS = " + NS + ", TPLL = " + TPLL + ", TPS = " + Arrays.toString(TPS));
            int minIndex = this.getMinThreadIndex();
            if (TPLL.compareTo(TPS[minIndex]) <= 0) { //TPLL <= TPS[minIndex]
                //LLEGADA
                T = TPLL;
                NT = NT + 1;
                this.generateIA();
                TPLL = T.add(BigInteger.valueOf(IA)); //T + IA;
                if (NS != MC) {
                    STLL = STLL.add(T);
                    NS = NS + 1;
                    if (NS <= N) {
                        int freeIndex = findFreeThreadIndex();
                        this.generateTA();
                        TPS[freeIndex] = T.add(BigInteger.valueOf(TA)); //T + TA;
                        STA = STA.add(BigInteger.valueOf(TA)); //STA + TA;
                    }
                    logger.info("EVENT: " + NT + "º ARRIVE");
                } else {
                    ARR = ARR + 1;
                    logger.info("ONE REGRET");
                }
            } else {
                //SALIDA
                T = TPS[minIndex];
                STS = STS.add(T); //STS + T;
                NS = NS - 1;
                if (NS >= N) {
                    this.generateTA();
                    TPS[minIndex] = T.add(BigInteger.valueOf(TA)); //T + TA;
                    STA = STA.add(BigInteger.valueOf(TA)); //STA + TA;
                } else {
                    TPS[minIndex] = HV;
                }
                logger.info("EVENT: EXIT");
            }
            if ((NS != 0) && (T.compareTo(TF) >= 0)) //T>=TF
                TPLL = HV;
            logger.info("END: T = " + T + ", NS = " + NS + ", TPLL = " + TPLL + ", TPS = " + Arrays.toString(TPS));
            logger.info("--------------END-----------------------");
        }
    }
    //endregion
}