/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Compilador;

import java.util.ArrayList;

/**
 *
 * @author Patricia Carmona
 */
public class Tabela {

    private ArrayList<Constantes> listaConstantes;
    private ArrayList<Funcoes> listaFuncoes;
    private ArrayList<ChamadaFuncao> chamadaFuncoes;
    private Constantes constanteAtual;
    private Funcoes funcoesAtual;
    private Variaveis variavelAtual;
    private ChamadaFuncao chamadaAtual;
    private ArrayList<Variaveis> listaVariaveis;
    private ArrayList<String> listaErrosSemanticos;
    private ArrayList<String> Ides_ConstVarGlobal;
    private ArrayList<Variaveis> listaVariaveisFun;
    private ArrayList<Token> parChamada;

    public Tabela() {
        listaConstantes = new ArrayList<>();
        listaVariaveis = new ArrayList<>();
        listaFuncoes = new ArrayList<>();
        listaErrosSemanticos = new ArrayList<>();
        Ides_ConstVarGlobal = new ArrayList<>();
        chamadaFuncoes = new ArrayList<>();
        listaVariaveisFun = new ArrayList<>();
    }

    public void iniciarSemantico() {
        verificaTipoConstante();
        verificaTipoRetorno();
        buscaMain();
        verificaChamadaFun();
    }

    // cria uma nova constante
    public void addconst() {
        constanteAtual = new Constantes();
    }

    // adc um tipo a constante criada
    public void setTipoconst(Token atual) {
        constanteAtual.setTipo(atual);
    }

    // adc um identificador a contante criada
    public void setIdeconst(Token atual) {
        constanteAtual.setIde(atual);
    }

    // adc um valor a constante criada
    public void setValorconst(Token atual) {
        constanteAtual.setValor(atual);
    }

    // adc a contante a lista de constantes
    public void addListaconst() {
        if (!buscaConstante(constanteAtual)) {
            listaConstantes.add(constanteAtual);
        }
        // adc a constante a lista de identificadores
        ideRepetido(constanteAtual.getIde());
    }

    // imprime a lista de constantes, não foi usado.. apenas testes
    public void imprimeConst() {
        for (int i = 0; i < listaConstantes.size(); i++) {
            System.out.println(listaConstantes.get(i).getTipo().getLexema() + " "
                    + listaConstantes.get(i).getIde().getLexema() + " "
                    + listaConstantes.get(i).getValor().getLexema() + "\n");
        }
    }

    // verifica se o tipo das constantes é igual ao tipo do valor atribuído
    public void verificaTipoConstante() {
        for (int i = 0; i < listaConstantes.size(); i++) {
            if (!(listaConstantes.get(i).getValor().getTipo().equals("IDE"))) {
                if (!(listaConstantes.get(i).getTipo().getLexema()
                        .equals(listaConstantes.get(i).getValor().getTipo()))) {
                    addErro("O valor atribuido em '",
                            listaConstantes.get(i).getIde().getLexema(),
                            "' é diferente ao tipo declarado na linha ",
                            listaConstantes.get(i).getTipo().getLinha(), "");
                }
            } else {
                int j = 0;
                while (!(listaConstantes.get(j).getIde().getLexema()
                        .equals(listaConstantes.get(i).getValor().getLexema()))) {
                    j++;
                }
                if (!(listaConstantes.get(i).getTipo().getLexema()
                        .equals(listaConstantes.get(j).getTipo().getLexema()))) {
                    addErro("O valor atribuido em '",
                            listaConstantes.get(i).getIde().getLexema(),
                            "' é diferente ao tipo declarado na linha ",
                            listaConstantes.get(i).getTipo().getLinha(), "");
                }

            }
        }

    }

    // cria uma nova variavel
    public void addvar() {
        variavelAtual = new Variaveis();
    }

    // adc o tipo da variavel
    public void setTipovar(Token atual) {
        variavelAtual.setTipo(atual);
    }

    // adc o identificador da variavel
    public void setIdevar(Token atual) {
        variavelAtual.setIde(atual);
    }

    // adiciona a variavel a lista de variaveis
    public void addListavar() {
        /*
         * if(buscaVariavel(variavelAtual))
         * addErro("A variavel '",
         * variavelAtual.getIde().getLexema(),
         * "' na linha ",
         * variavelAtual.getIde().getLinha(),
         * " já foi declarada");
         * else{
         */
        listaVariaveis.add(variavelAtual);
        ideRepetido(variavelAtual.getIde());
        // }
    }

    public void imprimeVar() {
        for (int i = 0; i < listaVariaveis.size(); i++) {
            System.out.println(listaVariaveis.get(i).getTipo().getLexema() + " "
                    + listaVariaveis.get(i).getIde().getLexema() + "\n");
        }
    }

    public void addFun() {
        funcoesAtual = new Funcoes();
    }

    public void setTipoFun(Token atual) {
        funcoesAtual.setTipo(atual);
    }

    public void setIdeFun(Token atual) {
        funcoesAtual.setIde(atual);
    }

    public void setRetornoFun(Token atual) {
        funcoesAtual.setRetorno(atual);
    }

    public void setRetornoFunAtr(Token atual) {
        funcoesAtual.setRetornoAtribuido(atual);
    }

    public void getRetornoFunAtr() {
        funcoesAtual.getRetornoAtribuido();
    }

    public void addParametroFun() {
        listaVariaveisFun.add(variavelAtual);

    }

    public void addListaFun() {
        if (buscaFuncao(funcoesAtual)) {
            addErro("A função '",
                    funcoesAtual.getIde().getLexema(),
                    "' na linha ",
                    funcoesAtual.getIde().getLinha(),
                    " já foi declarada");
        } else {
            funcoesAtual.setListaVariaveis(listaVariaveisFun);
            listaFuncoes.add(funcoesAtual);
        }
    }

    public void addChamadaFun() {
        chamadaAtual = new ChamadaFuncao();
    }

    public void setIdeChamadaFun(Token atual) {
        chamadaAtual.setIde(atual);
    }

    public void novoParChamada() {
        parChamada = new ArrayList<>();
    }

    public void addParChamada(Token atual) {
        parChamada.add(atual);
    }

    public void addListaChamadaFun() {
        chamadaAtual.setParChamada(parChamada);
        chamadaFuncoes.add(chamadaAtual);
    }

    public void imprimeFun() {
        for (int i = 0; i < listaFuncoes.size(); i++) {
            System.out.println(listaFuncoes.get(i).getTipo().getLexema() + " "
                    + listaFuncoes.get(i).getIde().getLexema() + " "
                    + listaFuncoes.get(i).getRetorno().getLexema() + " "
                    + listaFuncoes.get(i).getRetornoAtribuido().getLexema() + "\n");

        }

    }

    public void verificaChamadaFun() {
        ChamadaFuncao a;

        for (int i = 0; i < chamadaFuncoes.size(); i++) {
            a = chamadaFuncoes.get(i);

            Funcoes aux_funcao;
            aux_funcao = new Funcoes();
            aux_funcao.setIde(a.getIde());

            if (buscaFuncao(aux_funcao)) {
                int j = 0;

                while (!(listaFuncoes.get(j).getIde().getLexema().equals(a.getIde().getLexema()))) {
                    j++;
                }
                aux_funcao = listaFuncoes.get(j);
                coisaFuncao(a, aux_funcao);

            } else {
                addErro("A função '", a.getIde().getLexema(),
                        "' chamada na linha ", a.getIde().getLinha(),
                        " não foi declarada");
            }
        }
    }

    public void coisaFuncao(ChamadaFuncao a, Funcoes aux_funcao) {

        if (aux_funcao.getParametros().size() == a.getParChamada().size()) {
            // System.out.println(listaFuncoes.get(j).getParametros().size() + " " +
            // a.getParChamada().size());
            for (int i = 0; i < aux_funcao.getParametros().size(); i++) {
                if (a.getParChamada().get(i).getTipo().equals("IDE")) {
                    int p = 0;
                    Variaveis v = new Variaveis();
                    v.setIde(a.getParChamada().get(i));
                    if (buscaVariavel(v)) {
                        while (!(listaVariaveis.get(p).getIde().getLexema().equals(v.getIde().getLexema()))) {
                            p++;
                        }
                        v = listaVariaveis.get(p);

                        if (!(aux_funcao.getParametros().get(i).getTipo().getLexema()
                                .equals(v.getTipo().getLexema()))) {

                            addErro("O parametro na chamada da função '", a.getIde().getLexema(),
                                    "' na linha ", a.getIde().getLinha(),
                                    " está com tipo incorreto");
                        }
                    } else {
                        Constantes c = new Constantes();
                        c.setIde(a.getParChamada().get(i));
                        if (buscaConstante(c)) {
                            while (!(listaConstantes.get(p).getIde().getLexema().equals(c.getIde().getLexema()))) {
                                p++;
                            }
                            c = listaConstantes.get(p);
                            if (!(aux_funcao.getParametros().get(i).getTipo().getLexema()
                                    .equals(c.getTipo().getLexema()))) {
                                addErro("O parametro na chamada da função '", a.getIde().getLexema(),
                                        "' na linha ", a.getIde().getLinha(),
                                        " está com tipo incorreto");
                            }
                        }
                    }
                } else {
                    if (!(aux_funcao.getParametros().get(i).getTipo().getLexema()
                            .equals(a.getParChamada().get(i).getTipo()))) {
                        addErro("O parametro na chamada da função '", a.getIde().getLexema(),
                                "' na linha ", a.getIde().getLinha(),
                                " está com tipo incorreto");
                    }
                }
            }
        } else {
            addErro("A qtd de parametros na chamada de função '", a.getIde().getLexema(),
                    "' na linha ", a.getIde().getLinha(),
                    " está incorreta");
        }
    }

    public ArrayList<String> getErros() {
        return listaErrosSemanticos;
    }

    public void addErro(String erro, String erro1, String erro2, String erro3, String erro4) {
        listaErrosSemanticos.add("Erro Semântico: " + erro + erro1 + erro2 + erro3 + erro4 + ".");
    }

    public boolean buscaConstante(Constantes c) {
        for (int j = 0; j < listaConstantes.size(); j++) {
            if ((listaConstantes.get(j).getIde().getLexema().equals(c.getIde().getLexema()))) {
                // System.out.println(c.getIde().getLexema());
                return true;
            }
        }
        return false;

    }

    public boolean buscaVariavel(Variaveis a) {
        for (int j = 0; j < listaVariaveis.size(); j++) {
            if ((listaVariaveis.get(j).getIde().getLexema().equals(a.getIde().getLexema()))) {
                return true;
            }
        }
        return false;
    }

    public boolean buscaFuncao(Funcoes f) {
        for (int j = 0; j < listaFuncoes.size(); j++) {
            if ((listaFuncoes.get(j).getIde().getLexema().equals(f.getIde().getLexema()))) {
                return true;
            }
        }
        return false;
    }

    // Método que verifica se o código recebido possui uma função chamada Main
    // Usada após receber todas as funções, faz uma varredura na lista de funções
    // verificando o nome delas
    // Usada na clase tabela
    // Funcionando
    public void buscaMain() {
        boolean main = false;
        for (int j = 0; j < listaFuncoes.size(); j++) {
            if ((listaFuncoes.get(j).getIde().getLexema().equals("main"))) {
                main = true;
                return;
            } else {
                main = false;
            }
        }
        if (!main) {
            addErro("A Função 'main' deve ser declarada - ", "Função 'main' não encontrada", "", "", "");
        }
    }

    public void ideRepetido(Token t) {

        for (int j = 0; j < Ides_ConstVarGlobal.size(); j++) {
            if ((Ides_ConstVarGlobal.get(j).equals(t.getLexema()))) {
                addErro("O identificador '", t.getLexema(), "' na linha ", t.getLinha(),
                        " já foi utilizado em outra declaração Global");
                return;
            }
        }
        Ides_ConstVarGlobal.add(t.getLexema());

    }

    public void VerificarIdeDeclarado(Token atual) {

        Constantes c = new Constantes();
        c.setIde(atual);
        c.setTipo(null);
        c.setValor(null);

        Variaveis v = new Variaveis();
        v.setIde(atual);
        v.setTipo(null);

        if (!(buscaConstante(c) || buscaVariavel(v))) {
            addErro("O identificador '", atual.getLexema(),
                    "' da linha ", atual.getLinha(),
                    " não foi declarado");
        }
    }

    // Método que verifica se o retorno da função foi declarado,
    // Esse método é chamado na Classe Analise Sintática no momento que recebe o
    // retorno
    // Funcionando
    public void verificaRetornoDeclarado(Token atual) {

        Constantes c = new Constantes();
        Variaveis v = new Variaveis();
        c.setIde(atual);
        c.setTipo(null);
        c.setValor(null);
        v.setIde(atual);
        v.setTipo(null);

        if (buscaConstante(c)) {
            addErro("O retorno na linha ", c.getIde().getLinha(),
                    " é inválido, pois não é permitida a atribuição de constante fora do bloco de contantes", "", "");
        } else if (!(buscaVariavel(v))) {
            addErro("A variável '", v.getIde().getLexema(),
                    "' da linha ", v.getIde().getLinha(),
                    " usada no retorno, não foi declarada");
        }
    }

    // Verifica se o identificador recebido é declarado como inteiro
    // Esse método é chamado na Classe Analise Sintática quando recebe o indice de
    // uma matriz
    // Funcionando
    public void verificaInteiro(Token atual) {

        Constantes c = new Constantes();
        Variaveis v = new Variaveis();
        c.setIde(atual);
        c.setTipo(null);
        c.setValor(null);
        v.setIde(atual);
        v.setTipo(null);

        if (buscaConstante(c)) {
            int j = 0;
            while (!(listaConstantes.get(j).getIde().getLexema().equals(c.getIde().getLexema()))) {
                j++;
            }
            if (!(listaConstantes.get(j).getTipo().getLexema().equals("int"))) {
                addErro("Esperado um valor inteiro na linha ", c.getIde().getLinha(), "", "", "");
            }
        } else if (buscaVariavel(v)) {
            int j = 0;
            while (!(listaVariaveis.get(j).getIde().getLexema().equals(v.getIde().getLexema()))) {
                j++;
            }
            if (!(listaVariaveis.get(j).getTipo().getLexema().equals("int"))) {
                addErro("Esperado um valor inteiro na linha ", v.getIde().getLinha(), "", "", "");
            }
        } else {
            addErro("IDE não encontrado na linha ", c.getIde().getLinha(), "", "", "");
        }
    }

    private void atribuicaoRetorno() {

        Variaveis v = new Variaveis();
        for (int j = 0; j < listaFuncoes.size(); j++) {
            if (listaFuncoes.get(j).getRetornoAtribuido().getTipo().equals("IDE")) {

                v.setIde(listaFuncoes.get(j).getRetornoAtribuido());
                v.setTipo(null);
                VerificarIdeDeclarado(v.getIde());

                int i = 0;
                if (buscaVariavel(v)) {
                    while (!(listaVariaveis.get(i).getIde().getLexema().equals(v.getIde().getLexema()))) {
                        i++;
                    }
                    if (!(listaVariaveis.get(i).getTipo().getLexema()
                            .equals(listaFuncoes.get(j).getTipo().getLexema()))) {
                        addErro("Tipo da atribuição do Retorno na linha ",
                                listaFuncoes.get(j).getRetornoAtribuido().getLinha(),
                                " é inválido, pois a variavel '",
                                listaVariaveis.get(i).getIde().getLexema(),
                                "' é de tipo diferente do que foi declarado na função");
                    }

                }
            } else if (listaFuncoes.get(j).getRetornoAtribuido().getLexema().equals("true")
                    || listaFuncoes.get(j).getRetornoAtribuido().getLexema().equals("false")) {

                if (!(listaFuncoes.get(j).getTipo().getLexema().equals("boolean"))) {
                    addErro("Tipo da atribuição do Retorno na linha ",
                            listaFuncoes.get(j).getRetornoAtribuido().getLinha(),
                            " é inválido, pois o resultado '",
                            listaFuncoes.get(j).getRetornoAtribuido().getLexema(),
                            "' é de tipo diferente do que foi declarado na função");
                }
            } else if (listaFuncoes.get(j).getRetornoAtribuido().getTipo().equals("string")
                    || listaFuncoes.get(j).getRetornoAtribuido().getTipo().equals("int")
                    || listaFuncoes.get(j).getRetornoAtribuido().getTipo().equals("float")) {

                if (!(listaFuncoes.get(j).getTipo().getLexema()
                        .equals(listaFuncoes.get(j).getRetornoAtribuido().getTipo()))) {
                    addErro("Tipo da atribuição do Retorno na linha ",
                            listaFuncoes.get(j).getRetornoAtribuido().getLinha(),
                            " é inválido, pois o resultado '",
                            listaFuncoes.get(j).getRetornoAtribuido().getLexema(),
                            "' é de tipo diferente do que foi declarado na função");
                }
            }
        }

    }

    private void verificaTipoRetorno() {
        Variaveis v = new Variaveis();
        for (int j = 0; j < listaFuncoes.size(); j++) {
            v.setIde(listaFuncoes.get(j).getRetorno());
            v.setTipo(null);
            int i = 0;
            if (buscaVariavel(v)) {
                while (!(listaVariaveis.get(i).getIde().getLexema().equals(v.getIde().getLexema()))) {
                    i++;
                }
                if (!(listaVariaveis.get(i).getTipo().getLexema().equals(listaFuncoes.get(j).getTipo().getLexema()))) {
                    addErro("Tipo do Retorno na linha ",
                            listaFuncoes.get(j).getRetorno().getLinha(),
                            " é inválido, pois a variavel '",
                            listaVariaveis.get(i).getIde().getLexema(),
                            "' é de tipo diferente do que foi declarado na função");
                }

                atribuicaoRetorno();
            }

        }
    }

    public void verificaTipoAtribuicao(Token aux_tipo, Token aux_tipo_2) {

        Constantes c = new Constantes();
        c.setIde(aux_tipo);
        c.setTipo(null);
        c.setValor(null);

        if (buscaConstante(c)) {
            addErro("A constante '", c.getIde().getLexema(),
                    "' na linha ", c.getIde().getLinha(),
                    " não pode receber valor de atribuição fora do bloco de constantes");
        } else {
            verificaTipoOperacao(aux_tipo, aux_tipo_2);
        }

    }

    public void verificaTipoOperacao(Token aux_tipo, Token aux_tipo_2) {

        Constantes c = new Constantes();
        c.setIde(aux_tipo);

        Constantes c2 = new Constantes();
        c2.setIde(aux_tipo_2);

        Variaveis v = new Variaveis();
        v.setIde(aux_tipo);

        Variaveis v2 = new Variaveis();
        v2.setIde(aux_tipo_2);

        if (buscaVariavel(v)) {
            int i = 0;
            while (!(listaVariaveis.get(i).getIde().getLexema().equals(v.getIde().getLexema()))) {
                i++;
            }
            v = listaVariaveis.get(i);
        } else if (buscaConstante(c)) {
            int i = 0;
            while (!(listaConstantes.get(i).getIde().getLexema().equals(c.getIde().getLexema()))) {
                i++;
            }
            c = listaConstantes.get(i);
        }
        if (buscaVariavel(v2)) {

            int i = 0;
            while (!(listaVariaveis.get(i).getIde().getLexema().equals(v2.getIde().getLexema()))) {
                i++;
            }
            v2 = listaVariaveis.get(i);
        } else if (buscaConstante(c2)) {
            int i = 0;
            while (!(listaConstantes.get(i).getIde().getLexema().equals(c2.getIde().getLexema()))) {
                i++;
            }
            c2 = listaConstantes.get(i);
        }

        if (buscaVariavel(v) && buscaVariavel(v2)) {
            if (!(v.getTipo().getLexema().equals(v2.getTipo().getLexema()))) {
                addErro("O tipo do identificador '", aux_tipo_2.getLexema(),
                        "' na operação da linha ", aux_tipo_2.getLinha(),
                        " é diferente do tipo da operação");
            }
        } else if (buscaVariavel(v) && buscaConstante(c2)) {
            if (!(v.getTipo().getLexema().equals(c2.getTipo().getLexema()))) {
                addErro("O tipo do identificador '", aux_tipo_2.getLexema(),
                        "' na operação da linha ", aux_tipo_2.getLinha(),
                        " é diferente do tipo da operação");
            }
        } else if (buscaConstante(c) && buscaVariavel(v2)) {
            if (!(c.getTipo().getLexema().equals(v2.getTipo().getLexema()))) {
                addErro("O tipo do identificador '", aux_tipo_2.getLexema(),
                        "' na operação da linha ", aux_tipo_2.getLinha(),
                        " é diferente do tipo da operação");
            }
        } else if (buscaConstante(c) && buscaConstante(c2)) {
            if (!(c.getTipo().getLexema().equals(c2.getTipo().getLexema()))) {
                addErro("O tipo do identificador '", aux_tipo_2.getLexema(),
                        "' na operação da linha ", aux_tipo_2.getLinha(),
                        " é diferente do tipo da operação");
            }
        }

    }

    public void verificaTipoOperacaoTipos(Token aux_tipo, Token aux_tipo_2) {

        Variaveis v = new Variaveis();
        v.setIde(aux_tipo);

        Constantes c = new Constantes();
        c.setIde(aux_tipo);

        if (!(buscaConstante(c))) {
            if (buscaVariavel(v)) {
                int i = 0;
                while (!(listaVariaveis.get(i).getIde().getLexema().equals(v.getIde().getLexema()))) {
                    i++;
                }
                v = listaVariaveis.get(i);
                if (!(v.getTipo().getLexema().equals(aux_tipo_2.getTipo()))) {
                    addErro("O tipo do operando '", aux_tipo_2.getLexema(),
                            "' na operação da linha ", aux_tipo_2.getLinha(),
                            " é diferente do tipo da operação");
                }
            }

        } else {
            addErro("A constante '", aux_tipo.getLexema(),
                    "' na linha ", aux_tipo.getLinha(),
                    " não pode receber valor de atribuição fora do bloco de constantes");
        }
    }

    public void condicaoFor(Token aux_for, Token aux_for_1) {

        if ((aux_for_1.getTipo().equals("int")) || (aux_for_1.getTipo().equals("IDE"))) {

            Constantes c = new Constantes();
            c.setIde(aux_for);

            if (buscaConstante(c)) {
                addErro("A constante '", c.getIde().getLexema(),
                        "' na linha ", c.getIde().getLinha(),
                        " não pode receber valor de atribuição fora do bloco de constantes");
            } else {
                verificaTipoOperacao(aux_for, aux_for_1);
            }

        } else {
            addErro("A condição do for na linha ", aux_for.getLinha(),
                    " está incorreta, pois o contador '", aux_for_1.getLexema(),
                    "' não é um inteiro ou um identificador");
        }
    }
}