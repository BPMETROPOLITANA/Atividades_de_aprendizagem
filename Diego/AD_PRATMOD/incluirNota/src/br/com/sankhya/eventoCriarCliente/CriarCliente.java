package br.com.sankhya.eventoCriarCliente;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

public class CriarCliente implements EventoProgramavelJava {

    private String CNPJCPF;

    private final JapeWrapper parceiroDAO = JapeFactory.dao("Parceiro");

    private DynamicVO parceiroVO;
    private DynamicVO criadorNotaVO;

    @Override
    public void beforeInsert(PersistenceEvent pe) throws Exception {
        try{

            setCriadorNotaVO(pe);
            setCNPJCPFformatado();
            verificaCGC_CPF();
            setParceiroVO();

            if (!verificaSeParceiroExiste()){
                criaParceiro();
            }

        } catch(Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private void verificaCGC_CPF() throws Exception {
        try {
            if (this.CNPJCPF.length() != 11 && this.CNPJCPF.length() != 14) {
                throw new Exception("CNPJ / CPF de tamanho inválido");
            };
        }catch (Exception e) {
            throw new Exception("Erro ao validar CPF / CNPJ: "+e);
        }
    }

    private void criaParceiro() throws Exception {
        DynamicVO parceiroPadraoVO = retornaParceiroPadrao();
        try {
            parceiroDAO.create()
                    .set("CGC_CPF", this.CNPJCPF.trim())
                    .set("NOMEPARC", parceiroPadraoVO.asString("NOMEPARC"))
                    .set("TIPPESSOA", (this.CNPJCPF.trim().length() == 11) ? "F" : "J")
                    .set("CLIENTE", parceiroPadraoVO.asString("CLIENTE"))
                    .set("FORNECEDOR", parceiroPadraoVO.asString("FORNECEDOR"))
                    .set("IDENTINSCESTAD", parceiroPadraoVO.asString("IDENTINSCESTAD"))
                    .set("CODCID", parceiroPadraoVO.asBigDecimal("CODCID"))
                    .set("CEP", parceiroPadraoVO.asString("CEP"))
                    .save();
        } catch (Exception e){
            throw new Exception("Erro ao criar parceiro: "+e);
        }
    }

    private DynamicVO retornaParceiroPadrao() throws Exception {
        try{
            return this.parceiroDAO.findOne("REGEXP_REPLACE(CGC_CPF, '[^0-9]', '') = '00000000000' ");
        }catch (Exception e){
            throw new Exception("Erro ao buscar parceiro padrão: "+ e);
        }
    }

    private void setCNPJCPFformatado() throws Exception {
        try{
            this.CNPJCPF = criadorNotaVO.asString("CNPJCLI").replaceAll("[^0-9]", "");
        } catch (Exception e){
            throw new Exception("CNPJ / CPF inválido: "+ e);
        }
    }

    private void setParceiroVO() throws Exception {
        try {
            this.parceiroVO = this.parceiroDAO.findOne("REGEXP_REPLACE(CGC_CPF, '[^0-9]', '') = ? ", this.CNPJCPF);
        } catch (Exception e){
            throw new Exception("Erro ao buscar parceiro: "+e);
        }
    }

    private void setCriadorNotaVO(PersistenceEvent pe) throws Exception {
        try{
            this.criadorNotaVO = (DynamicVO) pe.getVo();
        } catch (Exception e){
            throw new Exception("Erro ao obter dados da Tabela: "+ e);
        }
    }

    private boolean verificaSeParceiroExiste() throws Exception {
        try {
            return this.parceiroVO != null;
        } catch (Exception e){
            throw new Exception("Erro ao validar se parceiro existe");
        }
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) {

    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) {

    }
}