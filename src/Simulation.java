import java.math.BigInteger;
import java.util.Random;
import java.util.Scanner;
import java.util.Arrays;

public class Simulation {

    //region VARIABLES
    //VARIABLES EXÓGENAS
    int IA; //Intervalo arribo búsqueda
    int TA; //Tiempo atención thread

    //VARIABLES ENDÓGENAS
    static int N; //Cantidad threads
    int NS; //Número búsquedas en el sistema

    //RESULTADOS
    int PPS; //Promedio permanencia sistema
    int PEC; //Promedio espera cola
    int PTA; //Promedio tiempo atención

    //AUXS RESULTADOS
    int NT; //Número total elementos en sistema
    BigInteger STLL; //Sumatoria tiempos de llegada
    BigInteger STS; //Sumatoria tiempos de salida
    BigInteger STA; //Sumatoria tiempos de atención

    //TABLA EVENTOS FUTUROS (T.E.F)
    BigInteger TPLL; //Tiempo próxima llegada
    BigInteger[] TPS; //Tiempo próxima salida

    //TIEMPOS
    BigInteger T; //Tiempo;
    static BigInteger TF; // Tiempo final
    BigInteger HV; //High Value

    //FUNCIÓN DENSIDAD DE PROBABILIDAD (F.D.P)
    //IA
    int startIA;
    int endIA;
    int maxIA;
    //TA
    int startTA;
    int endTA;
    int maxTA;
    //endregion

    //region MAIN
    public static void main(String[] args) {
        Simulation simulation = new Simulation();

        //region INGRESO
        Scanner in = new Scanner(System.in);
        System.out.println("Ingrese cantidad de puestos (threads): ");
        N = in.nextInt();
        System.out.println("Ingrese tiempo final (en ms): ");
        TF = in.nextBigInteger();
        //endregion

        simulation.run();
        simulation.calculateResults();
        simulation.printResults();
    }
    //endregion

    //region FUNCTIONS OPERATIONS
    public int functionIA(int x) {
        return (4 * x + 5); //FIXME: funcion lineal hardcodeada para testear
    }

    public int functionTA(int x) {
        return (4 * x + 5); //FIXME: funcion lineal hardcodeada para testear
    }
    //endregion

    //region GENERATORS OPERATIONS
    public void generateIA() {
        while (true) {
            double r1 = new Random().nextDouble();
            double r2 = new Random().nextDouble();

            int x1 = (int) ((endIA - startIA) * r1 + startIA);
            double y1 = (maxIA * r2);

            if (functionIA(x1) >= y1) {
                IA = x1;
                return;
            }
        }
    }

    public void generateTA() {
        while (true) {
            double r1 = new Random().nextDouble();
            double r2 = new Random().nextDouble();

            int x1 = (int) ((endTA - startTA) * r1 + startTA);
            double y1 = (maxTA * r2);

            if (functionTA(x1) >= y1) {
                TA = x1;
                return;
            }
        }
    }
    //endregion

    //region INITIALIZERS
    public void initConditions() {
        //Funciones
        startIA = 0;
        endIA = 999;
        maxIA = 1;

        startTA = 0;
        endTA = 999;
        maxTA = 1;

        //FIXME: El HV le asigno el tiempo final para el máximo tiempo de atención posible
        HV = TF.add(BigInteger.valueOf(endTA)); //TF + endTA

        T = BigInteger.valueOf(0);
        NS = 0;
        TPLL = BigInteger.valueOf(0);
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
    }

    public void printResults() {
        System.out.println("Promedio permanencia sistema: " + PPS);
        System.out.println("Promedio espera cola: " + PEC);
        System.out.println("Promedio tiempo atención: " + PTA);
    }
    //endregion OPERA

    //region RUNNER
    public void run() {
        this.initConditions();
        int minIndex = this.getMinThreadIndex();

        while (T.compareTo(TF) < 0) { //T<TF
            if (TPLL.compareTo(TPS[minIndex]) <= 0) { //TPLL <= TPS[minIndex]
                //Llegada
                T = TPLL;
                NT = NT + 1;
                STLL = STLL.add(TPLL);
                this.generateIA();
                TPLL = T.add(BigInteger.valueOf(IA)); //T + IA;
                NS = NS + 1;
                if (NS <= N) {
                    int freeIndex = findFreeThreadIndex();
                    this.generateTA();
                    TPS[freeIndex] = T.add(BigInteger.valueOf(TA)); //T + TA;
                    STA = STA.add(BigInteger.valueOf(TA)); //STA + TA;
                }
            } else {
                //Salida
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
            }
        }
    }
    //endregion
}
