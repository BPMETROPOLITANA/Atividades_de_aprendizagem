package br.com.metropolitana.pessoal.IncluiCNPJ;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import java.math.BigDecimal;

public class IncluiCNPJ implements EventoProgramavelJava {
    JapeWrapper parceiroDAO = JapeFactory.dao(DynamicEntityNames.PARCEIRO);
    DynamicVO parceiroVO = null;

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {

    }


    private void inserirParceiro(DynamicVO telaVo) throws Exception {
        parceiroVO = parceiroDAO.create()
                .set("NOMEPARC", "TESTE")
                .set("RAZAOSOCIAL", "TESTEEEE")
                .set("CGC_CPF", telaVo.asString("CNPJCLIENTE"))
                .set("CODCID", new BigDecimal(8))
                .set("TIPPESSOA", "J")
                .save();
    }

    public boolean naoExiste(DynamicVO telaVo) throws Exception {
        JapeWrapper parceiroDAO = JapeFactory.dao("Parceiro");
        DynamicVO parceiroVO = parceiroDAO.findOne(" CGC_CPF = ?"
                , telaVo.asString("CNPJCLIENTE"));
        return parceiroVO != null;
    }

    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        DynamicVO telaVo = (DynamicVO) event.getVo();
        telaVo.asString("CNPJCLIENTE");
        if (!naoExiste(telaVo)) {
            inserirParceiro(telaVo);
        }
    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
