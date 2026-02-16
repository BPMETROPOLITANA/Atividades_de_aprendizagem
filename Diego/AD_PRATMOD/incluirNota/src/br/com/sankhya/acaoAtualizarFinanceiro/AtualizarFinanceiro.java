package br.com.sankhya.acaoAtualizarFinanceiro;

import br.com.sankhya.acaoAtualizarFinanceiro.includes.Queries;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

public class AtualizarFinanceiro implements AcaoRotinaJava {

    private final JapeWrapper movFinDAO = JapeFactory.dao("Financeiro");
    private final JapeWrapper TGFCABDAO = JapeFactory.dao("CabecalhoNota");
    private final JapeWrapper usuDAO = JapeFactory.dao("Usuario");

    private Collection<DynamicVO> movFinVO;

    private final ArrayList<BigDecimal> nunotas = new ArrayList<>();

    @Override
    public void doAction(ContextoAcao sctx) {

        try {
            setArrayNunotasCriadorNotas();
            setMovFinVO();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar Financeiro: "+e);
        }
    }

    private void setMovFinVO() {
        this.nunotas.forEach(nunota -> {
            try{
                this.movFinVO = this.movFinDAO.find("NUNOTA = ?", nunota);
                atualizaFinanceiro();
            }catch (Exception e){
                throw new RuntimeException("Erro ao buscar VOs do financeiro: "+e);
            }
        });
    }

    private void atualizaFinanceiro() {
        this.movFinVO.forEach( movFinVO -> {
            try {
                String nomeUsuInc = retornaNomeUsuInc(movFinVO);
                movFinDAO.prepareToUpdate(movFinVO)
                        .set("HISTORICO", "Financeiros Gerados de Acordo com o Módulo de Práticas Java - Nome do Usuário inclusão: "+nomeUsuInc)
                        .update();
                System.out.println("Financeiro Atualiado: "+movFinVO);
            }catch (Exception e){
                throw new RuntimeException("Erro ao atualizar financeiro: "+e);
            }
        });
    }

    private String retornaNomeUsuInc(DynamicVO movFinVO) throws Exception {
        try {
            BigDecimal codUsuInc = retornaCodUsuInc(movFinVO);

            return this.usuDAO.findOne("CODUSU = ?", codUsuInc).asString("NOMEUSU");
        }catch (Exception e){
            throw new Exception("Erro ao buscar nome do usuário inclusão: "+e);
        }
    }

    private BigDecimal retornaCodUsuInc(DynamicVO movFinVO) throws Exception {
        try {
            return this.TGFCABDAO.findOne("NUNOTA = ? ", movFinVO.asBigDecimal("NUNOTA")).asBigDecimal("CODUSUINC");
        } catch (Exception e){
            throw new Exception("Erro ao bucar ID usuário de inclusão: "+e);
        }
    }

    private void setArrayNunotasCriadorNotas() throws Exception {
        try {
            ResultSet nunota = retornaQueryCriadorNotas();

            while (nunota.next()) {
                BigDecimal valorNunota = nunota.getBigDecimal("NUNOTA");
                this.nunotas.add(valorNunota);
            }
            System.out.println("setArrayNunotasCriadorNotas: "+ this.nunotas);
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
