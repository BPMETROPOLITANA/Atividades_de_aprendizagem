# AD_PRATMOD - Módulo de Práticas Java para Sankhya

Projeto desenvolvido como atividade de aprendizagem para o sistema Sankhya ERP, implementando funcionalidades de criação de notas fiscais, gerenciamento de clientes e atualização de registros financeiros.

## 📋 Índice

- [Visão Geral](#visão-geral)
- [Funcionalidades](#funcionalidades)

## 🎯 Visão Geral

Este módulo oferece três funcionalidades principais integradas ao Sankhya ERP:
1. **Inclusão automatizada de notas fiscais** através de uma tabela customizada e um botão de ação
2. **Criação automática de parceiros/clientes** através de um evento de tabela
3. **Atualização de registros financeiros** através de um evento agendado

## ⚙️ Funcionalidades

### 1. Incluir Nota (Ação de Botão)

**Classe:** `IncluirNota.java`  
**Pacote:** `br.com.sankhya.botaoIncluirNota`  
**Tipo:** Ação de Rotina Java (AcaoRotinaJava)

#### Descrição
Ação que cria notas fiscais automaticamente a partir de registros na tabela customizada `AD_PRATMOD`.

#### Funcionamento
- Busca todos os registros selecionados na tabela `AD_PRATMOD`
- Valida se já existe nota criada para os itens selecionados
- Valida se o valor unitário não é negativo e se quantidade é maior que 0
- Agrupa itens por cabeçalho de nota (baseado em: data de negociação, cliente, tipo de negociação, natureza, centro de custo, tipo de operação e empresa)
- Para cada grupo:
  - Cria o cabeçalho da nota fiscal (CabecalhoNota)
  - Adiciona os itens à nota (ItemNota)
      - Se já existir algum item com o mesmo código na nota, a quantidade é somada, evitando duplicação.
      - Se o valor unitário não for fornecido, busca automaticamente o último custo de reposição (CUSREP) do produto
  - Confirma a nota utilizando o helper CAC (CACHelper)
- Atualiza os registros na tabela `AD_PRATMOD` com usuário do faturamento, data/hora da criação da nota e Nº único da nota criada (NUNOTA).
- Em caso de erro, exclui automaticamente o cabeçalho criado (rollback)

---

### 2. Criar Cliente (Evento Programável)

**Classe:** `CriarCliente.java`  
**Pacote:** `br.com.sankhya.eventoCriarCliente`  
**Tipo:** Evento Programável Java (EventoProgramavelJava)

#### Descrição
Evento disparado antes da inserção de registros na tabela `AD_PRATMOD` que verifica e cria automaticamente parceiros/clientes quando necessário.

#### Funcionamento
- **Trigger:** beforeInsert (antes de inserir registro)
- Extrai e formata o CPF/CNPJ do campo CNPJCLI, removendo tudo que não é número e verifica a validade pelo tamanho 11 = CPF e 14 = CNPJ
- Busca se já existe um parceiro com aquele CPF/CNPJ
- Se o parceiro NÃO existir:
  - Busca um parceiro padrão (CPF: 00000000000) para usar como template
  - Cria novo parceiro com:
    - CGC_CPF do registro
    - Tipo de pessoa (F para CPF com 11 dígitos, J para CNPJ)
    - Demais dados copiados do parceiro padrão (nome, cidade, CEP, etc.)

#### Validações
- Remove caracteres não numéricos do CPF/CNPJ
- Define automaticamente tipo de pessoa baseado no tamanho do documento
- Previne duplicação de parceiros

---

### 3. Atualizar Financeiro (Ação Agendada)

**Classe:** `AtualizarFinanceiro.java`  
**Pacote:** `br.com.sankhya.acaoAtualizarFinanceiro`  
**Tipo:** Ação de Rotina Java (AcaoRotinaJava)

#### Descrição
Ação que atualiza o campo HISTORICO dos registros financeiros relacionados às notas criadas pelo módulo.

#### Funcionamento
1. Busca todos os números de nota (NUNOTA) da tabela `AD_PRATMOD`
2. Inclui também notas relacionadas através da tabela TGFVAR (variações)
3. Para cada nota encontrada:
   - Busca os registros financeiros relacionados
   - Obtém o código do usuário que incluiu a nota na TGFCAB (CODUSUINC)
   - Busca o nome do usuário
   - Atualiza o campo HISTORICO com a mensagem:
     ```
     "Financeiros Gerados de Acordo com o Módulo de Práticas Java - Nome do Usuário inclusão: [NOME_USUARIO]"
     ```

## 👤 Autor

Diego dos Santos Nunes
