package br.com.sankhya.acaoAtualizarFinanceiro;

import br.com.sankhya.acaoAtualizarFinanceiro.includes.Queries;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class AtualizarFinanceiro implements AcaoRotinaJava {

    private final JapeWrapper movFinDAO = JapeFactory.dao("Financeiro");
    private final JapeWrapper TGFCABDAO = JapeFactory.dao("CabecalhoNota");

    private Collection<DynamicVO> movFinVO;

    private final ArrayList<BigDecimal> nunotas = new ArrayList<>();

    @Override
    public void doAction(ContextoAcao sctx) {

        try {
            setArrayNunotasCriadorNotas();
            setMovFinVO();
            atualizaFinanceiro();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar Financeiro: "+e);
        }
    }

    private void setMovFinVO() throws Exception {
        try{
            this.movFinVO = this.movFinDAO.find("NUNOTA IN ?", this.nunotas.toArray());
        }catch (Exception e){
            throw new Exception("Erro ao buscar VO do financeiro: "+e);
        }
    }

    private void atualizaFinanceiro() {
        this.movFinVO.forEach( movFinVO -> {
            try {
                String codUsuInc = retornaCodUsuInc(movFinVO);
                movFinDAO.prepareToUpdate(movFinVO)
                        .set("HISTORICO", "Financeiros Gerados de Acordo com a Prática de Módulos Java - "+codUsuInc)
                        .update();
            }catch (Exception e){
                throw new RuntimeException("Erro ao atualizar financeiro: "+e);
            }
        });
    }

    private String retornaCodUsuInc(DynamicVO movFinVO) throws Exception {
        try {
            return this.TGFCABDAO.findOne("NUNOTA = ? ", movFinVO.asBigDecimal("NUNOTA")).asBigDecimal("CODUSUINC").toString();
        } catch (Exception e){
            throw new Exception("Erro ao bucar usuário de inclusão: "+e);
        }
    }

    private void setArrayNunotasCriadorNotas() throws Exception {
        try {
            ResultSet nunota = retornaQueryCriadorNotas();


            while (nunota.next()) {
                BigDecimal valorNunota = nunota.getBigDecimal("NUNOTA");
                this.nunotas.add(valorNunota);
            }
        }catch (Exception e){
            throw new Exception("Erro ao formar Array de NUNOTAS: "+e);
        }
    }

    private ResultSet retornaQueryCriadorNotas() throws Exception {
        try {
            Queries query = new Queries();
            NativeSql nativeSql = new NativeSql(EntityFacadeFactory.getDWFFacade().getJdbcWrapper());

            return nativeSql.executeQuery(query.queryRetornaNunotasNotas);
        } catch (Exception e){
            throw new Exception("Erro ao consultar NUNOTAS no criador de notas: "+e);
        }
    }
}
