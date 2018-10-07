
import java.math.BigInteger;
import java.util.logging.Logger;
import java.util.logging.FileHandler;

public class Variables {

    //region VARIABLES
    //LOGS
    static Logger logger;
    static Logger logResults;
    public FileHandler fhLogs;
    FileHandler fhResults;

    //VARIABLES EXÓGENAS
    int IA; //Intervalo arribo búsqueda
    int TA; //Tiempo atención thread

    //VARIABLES ENDÓGENAS
    //CONTROL
    static int N; //Cantidad threads
    static int MC; //Tamaño máximo cola
    int NS; //Número búsquedas en el sistema

    //RESULTADOS
    double PPS; //Promedio permanencia sistema
    double PEC; //Promedio espera cola
    int PTA; //Promedio tiempo atención
    double PR; //Porcentaje Rechazo

    //AUXS RESULTADOS
    int NT; //Número total elementos en sistema
    BigInteger STLL; //Sumatoria tiempos de llegada
    BigInteger STS; //Sumatoria tiempos de salida
    BigInteger STA; //Sumatoria tiempos de atención
    int ARR; //Cantidad arrepentimientos

    //TABLA EVENTOS FUTUROS (T.E.F)
    BigInteger TPLL; //Tiempo próxima llegada
    BigInteger[] TPS; //Tiempo próxima salida

    //TIEMPOS
    BigInteger T; //Tiempo;
    static BigInteger TF; // Tiempo final
    BigInteger HV; //High Value

    //FUNCIÓN DENSIDAD DE PROBABILIDAD (F.D.P)
    //TA
    double K_TA = 1232.2; //IA Function - K
    double LAMBDA_TA = 2.3746; //IA Function - Lambda
    double BETA_TA = 186.07; //IA  Function - Beta

    int startTA;
    int endTA;
    double maxTA;
    //endregion
}
/** VIVA EL ANIME WACHO!*/