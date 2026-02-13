package br.com.sankhya.acaoAtualizarFinanceiro.includes;

public class Queries {
    public final String queryRetornaNunotasNotas =
            "SELECT NUNOTA "
            +"FROM AD_PRATMOD "

            +"UNION "

            +"SELECT NUNOTA "
            +"FROM TGFVAR "
            +"WHERE NUNOTAORIG IN ( "
                +"SELECT NUNOTA "
                +"FROM AD_PRATMOD "
            +")";
}
