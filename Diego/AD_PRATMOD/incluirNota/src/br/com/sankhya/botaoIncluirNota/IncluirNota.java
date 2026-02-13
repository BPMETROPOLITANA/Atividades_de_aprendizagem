package br.com.sankhya.botaoIncluirNota;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.botaoIncluirNota.models.Item;
import br.com.sankhya.botaoIncluirNota.models.Nota;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.util.*;

public class IncluirNota implements AcaoRotinaJava {
    private EntityFacade dwfFacade;
    private ServiceContext serviceContext;
    private Collection<BigDecimal> idCriadorNotas;

    private DynamicVO nunotaModeloVO;
    private DynamicVO cabecalhoNotaCriadaVO;
    private DynamicVO itemVO;
    private Collection<DynamicVO> dadosNotasVOs;

    private final JapeWrapper criadorNotaDAO = JapeFactory.dao("AD_PRATMOD");
    private final JapeWrapper volumeProdDAO = JapeFactory.dao("VolumeAlternativo");
    private final JapeWrapper produtoDAO = JapeFactory.dao("Produto");
    private final JapeWrapper parceiroDAO = JapeFactory.dao("Parceiro");

    private final HashMap<String, Nota> mapaCabecalhosNota = new HashMap<>();

    private final CACHelper cacHelper = new CACHelper();

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        this.serviceContext = ServiceContext.getCurrent();
        this.dwfFacade = EntityFacadeFactory.getDWFFacade();

        try {
            this.idCriadorNotas = getIdsCriadorNotas(contextoAcao);
            this.dadosNotasVOs = retornaCriadoresNotaVO();

            if (notaJaCriada()) {
                contextoAcao.setMensagemRetorno("Algum dos itens selecionados já possui nota criada");
                return;
            }

            criaModeloNotas();
            criaNotas();
            insereUsuarioFaturamentoEDataCriacaoNota(contextoAcao);

        }catch (Exception e) {
            if (cabecalhoNotaCriadaVO != null) {
                deletaCabecalhoNota(this.cabecalhoNotaCriadaVO.asBigDecimal("NUNOTA"));
            }
            throw new Exception("ERRO AO CRIAR A NOTA: "+e);
        }
    }

    private void deletaCabecalhoNota(BigDecimal nunota) throws Exception {
        try {
            this.cacHelper.excluirNota(nunota);
        } catch (Exception e){
            throw new Exception("Erro ao excluir cabeçalho da nota: "+e);
        }
    }

    private void insereUsuarioFaturamentoEDataCriacaoNota(ContextoAcao contextoAcao) throws Exception {
        System.out.println("Usuário logado: "+contextoAcao.getUsuarioLogado());
        for(BigDecimal idTela: this.idCriadorNotas){
            this.criadorNotaDAO.prepareToUpdateByPK(idTela)
                    .set("CODUSUFATUR", contextoAcao.getUsuarioLogado())
                    .set("DHFATUR", TimeUtils.getNow())
                    .update();
        }
    }

    private void criaNotas() {
            this.mapaCabecalhosNota.forEach( (k, nota) -> {
                try {
                    System.out.println("Tentando montar Cabecalho Nota");
                    System.out.println("Data Pedido: " + nota.toString());
                    this.nunotaModeloVO = (DynamicVO) this.dwfFacade.getDefaultValueObjectInstance("CabecalhoNota");
                    this.nunotaModeloVO.setProperty("NUNOTA", null);
                    this.nunotaModeloVO.setProperty("CODCENCUS", nota.getCODCENCUS());
                    this.nunotaModeloVO.setProperty("CODNAT", nota.getCODNAT());
                    this.nunotaModeloVO.setProperty("CODPARC", this.parceiroDAO.findOne("REGEXP_REPLACE(CGC_CPF, '[^0-9]', '') = ? ", nota.getCGCCPF()).asBigDecimal("CODPARC"));
                    this.nunotaModeloVO.setProperty("CODEMP", nota.getCODEMP());
                    this.nunotaModeloVO.setProperty("CODTIPVENDA", nota.getCODTIPNEG());
                    this.nunotaModeloVO.setProperty("DHTIPVENDA", ComercialUtils.getTipoNegociacao(nota.getCODTIPNEG()).asTimestamp("DHALTER"));
                    this.nunotaModeloVO.setProperty("CODTIPOPER", nota.getCODTIPOPER());
                    DynamicVO topVO = ComercialUtils.getTipoOperacao(nota.getCODTIPOPER());
                    this.nunotaModeloVO.setProperty("DHTIPOPER", topVO.asTimestamp("DHALTER"));
                    this.nunotaModeloVO.setProperty("TIPMOV", topVO.asString("TIPMOV"));
                    this.nunotaModeloVO.setProperty("DTNEG", nota.getDTNEG());
                    this.nunotaModeloVO.setProperty("DTMOV", TimeUtils.getNow());
                    this.nunotaModeloVO.setProperty("OBSERVACAO", nota.getOBS());

                    insereCabecalhoNota();
                    montaItensNota(nota);

                }catch (Exception e){
                    try {
                        throw new Exception("Erro ao montar modelo de notas: "+ e);
                    } catch (Exception ex) {
                        throw new RuntimeException("Erro ao montar modelo de notas: "+ ex);
                    }
                }
            });
    }

    private void insereCabecalhoNota() throws Exception {
        try{
            PrePersistEntityState cabPreState = PrePersistEntityState.build(this.dwfFacade, "CabecalhoNota", this.nunotaModeloVO);
            BarramentoRegra barramentoRegra = this.cacHelper.incluirAlterarCabecalho(this.serviceContext, cabPreState);
            this.cabecalhoNotaCriadaVO = barramentoRegra.getState().getNewVO();
        } catch (Exception e){
            throw new Exception("Erro ao inserir nota: "+e);
        }
    }

    private void montaItensNota(Nota nota) throws Exception {
        try{
            for (Item item: nota.getItens()) {
                this.itemVO = (DynamicVO) this.dwfFacade.getDefaultValueObjectInstance("ItemNota");
                this.itemVO.setProperty("NUNOTA", this.cabecalhoNotaCriadaVO.asBigDecimal("NUNOTA"));
                this.itemVO.setProperty("CODPROD", item.getCODPROD());
                this.itemVO.setProperty("QTDNEG", item.getQTDNEG());
                DynamicVO produtoVO = produtoDAO.findByPK(item.getCODPROD());
                this.itemVO.setProperty("CODVOL", volumeProdDAO.findOne("CODPROD = ?", item.getCODPROD()).asString("CODVOL"));
                this.itemVO.setProperty("VLRUNIT", item.getVLRUNIT());
                this.itemVO.setProperty("CODLOCALORIG", produtoVO.asBigDecimal("CODLOCALPADRAO"));
                this.itemVO.setProperty("CONTROLE", null);
                this.itemVO.setProperty("PERCDESC", BigDecimal.valueOf(0.0D));
                this.itemVO.setProperty("VLRDESC", BigDecimal.valueOf(0.0D));
                this.itemVO.setProperty("BASEICMS", BigDecimal.valueOf(0.0D));
                this.itemVO.setProperty("VLRICMS", BigDecimal.valueOf(0.0D));
                this.itemVO.setProperty("ALIQICMS", BigDecimal.valueOf(0.0D));
                this.itemVO.setProperty("BASEIPI", BigDecimal.valueOf(0.0D));
                this.itemVO.setProperty("VLRIPI", BigDecimal.valueOf(0.0D));
                this.itemVO.setProperty("ALIQIPI", BigDecimal.valueOf(0.0D));

                insereNunotaTelaCriadorNotas(item);
                insereItemCabecalhoNota();
            }
        } catch (Exception e){
            throw new Exception("Erro ao inserir item da nota: "+e);
        }
    }

    private void insereItemCabecalhoNota() throws Exception {
        try{
            Collection<PrePersistEntityState> itensNota = new ArrayList<>();
            PrePersistEntityState itePreState = PrePersistEntityState.build(this.dwfFacade, "ItemNota", this.itemVO);
            itensNota.add(itePreState);
            this.cacHelper.incluirAlterarItem(this.cabecalhoNotaCriadaVO.asBigDecimal("NUNOTA"), this.serviceContext, itensNota,true);
        } catch (Exception e){
            throw new Exception("Erro em insereItemCabecalhoNota: "+e);
        }
    }

    private void insereNunotaTelaCriadorNotas(Item item) throws Exception {
        item.getID().forEach( id -> {
            try {
                criadorNotaDAO.prepareToUpdateByPK(id)
                        .set("NUNOTA", this.cabecalhoNotaCriadaVO.asBigDecimal("NUNOTA"))
                        .update();
            } catch (Exception e) {
                throw new RuntimeException("Erro em insereNunotaTelaCriadorNotas: " +e);
            }
        });
    }

    private boolean notaJaCriada() {
        for (DynamicVO criadorNotaVO: this.dadosNotasVOs){
            if(criadorNotaVO.asString("NUNOTA")!= null && !criadorNotaVO.asString("STATUSFATUR").equals("Em faturamento")){
                return true;
            }
        }
        return false;
    }

    private void criaModeloNotas() throws Exception {

        for (DynamicVO dadosNotaVO: this.dadosNotasVOs){

            String CGCCPFNormalizado = removeLetrasECarecteresEspeciais(dadosNotaVO.asString("CNPJCLI"));
            verificaCGCCPF(CGCCPFNormalizado);

            String identificacaoNota = dadosNotaVO.asTimestamp("DTNEG").toString()+"_"
                    +dadosNotaVO.asString("CNPJCLI")+"_"
                    +dadosNotaVO.asBigDecimal("CODTIPVENDA").toString()+"_"
                    +dadosNotaVO.asBigDecimal("CODNAT").toString()+"_"
                    +dadosNotaVO.asBigDecimal("CODCENCUS").toString()+"_"
                    +dadosNotaVO.asBigDecimal("CODTIPOPER").toString()+"_"
                    +dadosNotaVO.asBigDecimal("CODEMP").toString();

            Item objItemCriado = retornaObjItemCriado(dadosNotaVO);

            this.mapaCabecalhosNota.computeIfAbsent(identificacaoNota, k -> new Nota(
                    dadosNotaVO.asTimestamp("DTNEG"),
                    dadosNotaVO.asString("CNPJCLI").replaceAll("[^0-9]", ""),
                    dadosNotaVO.asBigDecimal("CODTIPVENDA"),
                    dadosNotaVO.asBigDecimal("CODNAT"),
                    dadosNotaVO.asBigDecimal("CODCENCUS"),
                    dadosNotaVO.asBigDecimal("CODTIPOPER"),
                    dadosNotaVO.asBigDecimal("CODEMP"),
                    dadosNotaVO.asString("OBSERVACAO")
            )).addItens(objItemCriado);
        }
    }

    private Item retornaObjItemCriado(DynamicVO dadosNotaVO) throws Exception {
        return new Item(
            dadosNotaVO.asBigDecimal("ID"),
            dadosNotaVO.asBigDecimal("CODPROD"),
            dadosNotaVO.asBigDecimal("VLRUNIT"),
            dadosNotaVO.asBigDecimal("QTDNEG")
        );
    }

    private void verificaCGCCPF(String CGCCPF) throws Exception {
        if( !(CGCCPF.length() >= 11 && CGCCPF.length() <= 14) ){
            throw new Exception("CNPJ ou CPF inválido!");
        }
    }

    private String removeLetrasECarecteresEspeciais(String s) {
        return s.replaceAll("[^0-9]", "");
    }

    private Collection<DynamicVO> retornaCriadoresNotaVO() throws Exception {
        Collection <DynamicVO> CriadoresNotaVO = new ArrayList<>();
        for (BigDecimal idTelaArquivo: this.idCriadorNotas) {
            CriadoresNotaVO.add(this.criadorNotaDAO.findByPK(idTelaArquivo));
        }
        return CriadoresNotaVO;
    }

    private Collection<BigDecimal> getIdsCriadorNotas(ContextoAcao contexto) throws Exception {
        Collection <BigDecimal> idTelaArquivo = new ArrayList<>();
        Registro[] linhas = contexto.getLinhas();
        if (linhas.length == 0) {
            throw new Exception("Nenhuma linha selecionada");
        }
        for (Registro linha: linhas) {
            idTelaArquivo.add(new BigDecimal(linha.getCampo("ID").toString()));
        }
        return idTelaArquivo;
    }
}