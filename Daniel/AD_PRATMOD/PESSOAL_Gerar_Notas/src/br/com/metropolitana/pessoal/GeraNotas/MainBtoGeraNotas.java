package br.com.metropolitana.pessoal.GeraNotas;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;
import com.sankhya.util.TimeUtils;


public class MainBtoGeraNotas implements AcaoRotinaJava {
    ContextoAcao contexto;
    private ServiceContext serviceContext;
    private DynamicVO nunotaModeloVO;
    private DynamicVO cabecalhoNotaCriadaVO;
    private DynamicVO itemVO;
    JapeWrapper produtoDAO = JapeFactory.dao(DynamicEntityNames.PRODUTO);


    private EntityFacade dwfFacade;
    private boolean isErro = false;
    private CACHelper cacHelper = new CACHelper();


    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        this.serviceContext = ServiceContext.getCurrent();
        this.dwfFacade = EntityFacadeFactory.getDWFFacade();
        JapeWrapper importaDAO = JapeFactory.dao("AD_PRATMOD");
        Registro[] linhas = contexto.getLinhas();

        for (Registro linha : linhas) {
            try {
                DynamicVO telaVO = importaDAO.findByPK((BigDecimal) linha.getCampo("ID"));
                if (!isErro) {
                    montaCabecalhoNota(linha);
                    insereCabecalhoNota();
                    montaItemNota(linha);
                    insereItemCabecalhoNota();
                    importaDAO.prepareToUpdate(telaVO)
                            .set("NUNOTA", cabecalhoNotaCriadaVO.asBigDecimal("NUNOTA"))
                            .update();
                    contexto.setMensagemRetorno("Nota criada com sucesso");
                }

            } catch (Exception e) {
                throw new RuntimeException("Erro ao gerar nota: " +e);
            }
        }
    }

    private void montaCabecalhoNota(Registro linha) throws Exception {
        DynamicVO tpvVO = ComercialUtils.getTipoNegociacao((BigDecimal) linha.getCampo("CODTIPVENDA"));
        DynamicVO topVO = ComercialUtils.getTipoOperacao((BigDecimal) linha.getCampo("CODTIPOPER"));
        this.nunotaModeloVO = (DynamicVO) this.dwfFacade.getDefaultValueObjectInstance("CabecalhoNota");
        this.nunotaModeloVO.setProperty("CODNAT", (BigDecimal) linha.getCampo("CODNAT"));
        this.nunotaModeloVO.setProperty("CODPARC", (BigDecimal) linha.getCampo("CODPARC"));
        this.nunotaModeloVO.setProperty("CODEMP", (BigDecimal) linha.getCampo("CODEMP"));
        this.nunotaModeloVO.setProperty("CODTIPOPER", (BigDecimal) linha.getCampo("CODTIPOPER"));
        this.nunotaModeloVO.setProperty("DHTIPOPER", (Timestamp) topVO.asTimestamp("DHALTER"));
        this.nunotaModeloVO.setProperty("CODTIPVENDA", (BigDecimal) linha.getCampo("CODTIPVENDA"));
        this.nunotaModeloVO.setProperty("DHTIPVENDA", (Timestamp) tpvVO.asTimestamp("DHALTER"));
        this.nunotaModeloVO.setProperty("TIPMOV", topVO.asString("TIPMOV"));
        this.nunotaModeloVO.setProperty("DTNEG", (Timestamp) linha.getCampo("DTNEG"));
        this.nunotaModeloVO.setProperty("DTMOV", (Timestamp) TimeUtils.getNow());
        this.nunotaModeloVO.setProperty("OBSERVACAO", linha.getCampo("OBSERVACAO"));
    }

    private void insereCabecalhoNota() throws Exception {
        try {
            Collection<PrePersistEntityState> cabNota = new ArrayList<>();
            PrePersistEntityState cabPreState = PrePersistEntityState.build(this.dwfFacade, "CabecalhoNota",
                    this.nunotaModeloVO);
            cabPreState.getNewVO();
            cabNota.add(cabPreState);
            BarramentoRegra barramentoRegra = this.cacHelper.incluirAlterarCabecalho(this.serviceContext, cabPreState);
            this.cabecalhoNotaCriadaVO = barramentoRegra.getState().getNewVO();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao inserir cabeçalho da nota: " +e);
        }
    }

    private void montaItemNota(Registro linha) throws Exception {
        this.itemVO = (DynamicVO) this.dwfFacade.getDefaultValueObjectInstance("ItemNota");
        DynamicVO produtoVO = produtoDAO.findByPK((BigDecimal) linha.getCampo("CODPROD"));
        this.itemVO.setProperty("NUNOTA", this.cabecalhoNotaCriadaVO.asBigDecimal("NUNOTA"));
        this.itemVO.setProperty("SEQUENCIA", new BigDecimal(1));
        this.itemVO.setProperty("CODPROD", (BigDecimal) linha.getCampo("CODPROD"));
        this.itemVO.setProperty("QTDNEG", new BigDecimal(6));
        this.itemVO.setProperty("CODVOL", produtoVO.asString("CODVOL"));
        this.itemVO.setProperty("VLRUNIT", (BigDecimal) linha.getCampo("VLRUNIT"));
        this.itemVO.setProperty("CODLOCALORIG", produtoVO.asBigDecimal("CODLOCALPADRAO") != null ? produtoVO.asBigDecimal("CODLOCALPADRAO") : new BigDecimal(0));
        this.itemVO.setProperty("CONTROLE", null);
        this.itemVO.setProperty("PERCDESC", BigDecimal.valueOf(0.0D));
        this.itemVO.setProperty("VLRDESC", BigDecimal.valueOf(0.0D));
        this.itemVO.setProperty("BASEICMS", BigDecimal.valueOf(0.0D));
        this.itemVO.setProperty("VLRICMS", BigDecimal.valueOf(0.0D));
        this.itemVO.setProperty("ALIQICMS", BigDecimal.valueOf(0.0D));
        this.itemVO.setProperty("BASEIPI", BigDecimal.valueOf(0.0D));
        this.itemVO.setProperty("VLRIPI", BigDecimal.valueOf(0.0D));
        this.itemVO.setProperty("ALIQIPI", BigDecimal.valueOf(0.0D));
    }

    private void insereItemCabecalhoNota() throws Exception {
        Collection<PrePersistEntityState> itensNota = new ArrayList<>();
        PrePersistEntityState itePreState = PrePersistEntityState.build(this.dwfFacade, "ItemNota", this.itemVO);
        itePreState.getNewVO();
        itensNota.add(itePreState);
        this.cacHelper.incluirAlterarItem(this.cabecalhoNotaCriadaVO.asBigDecimal("NUNOTA"), this.serviceContext, itensNota,
                true);
    }
}