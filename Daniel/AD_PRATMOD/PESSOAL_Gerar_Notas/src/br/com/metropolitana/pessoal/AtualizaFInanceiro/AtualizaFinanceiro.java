package br.com.metropolitana.pessoal.AtualizaFInanceiro;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.dwfdata.vo.tsi.UsuarioVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import java.math.BigDecimal;
import java.util.Collection;

public class AtualizaFinanceiro implements AcaoRotinaJava {
    JapeWrapper financeiroDAO = JapeFactory.dao(DynamicEntityNames.FINANCEIRO);
    JapeWrapper importaDAO = JapeFactory.dao("AD_PRATMOD");
    DynamicVO financeiroVO = null;
    JapeWrapper usuDAO = JapeFactory.dao("Usuario");
    String nomeUsu = "";
    BigDecimal nuNota = null;

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Collection<DynamicVO> telaVO = importaDAO.find("");
        for(DynamicVO tela : telaVO) {
            nuNota = tela.asBigDecimal("NUNOTA");
            System.out.println("O NUNOTA É" + nuNota);
            if(nuNota != null) {
                DynamicVO financeiro = buscaFinanceiro(nuNota);
                if(financeiro != null) {
                    atualizarHistorico(financeiro);
                }
            }
        }
    }

    public DynamicVO buscaFinanceiro(BigDecimal nuNota) throws Exception {
        financeiroVO = financeiroDAO.findOne(" NUNOTA = ?"
                , nuNota);
        return financeiroVO;
    }

    public void buscaNomeUsu() throws Exception {
        UsuarioVO usuarioVO = AuthenticationInfo.getCurrent().getUsuVO();
        this.nomeUsu = usuarioVO.getNOMEUSU();
    }

    public void atualizarHistorico(DynamicVO financeiro) throws Exception {
        try
            {
                buscaNomeUsu();
                financeiroDAO.prepareToUpdate(financeiro)
                        .set("HISTORICO", "Financeiros Gerados de Acordo com a Prática de Módulos Java – " + this.nomeUsu)
                        .update();
                System.out.println("Financeiro atualizado");
            } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar o Financeiro: " + e);
        }
    }
}
