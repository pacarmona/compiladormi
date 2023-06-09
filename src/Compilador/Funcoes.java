
package Compilador;

import java.util.ArrayList;

/**
 *
 * @author Patricia Carmona
 */
public class Funcoes {
    private Token tipo;
    private Token ide;
    private Token retorno, retorno_atribuido;

    public Variaveis variavelAtual;
    private ArrayList<Variaveis> listaVariaveis;

    public Funcoes() {
        listaVariaveis = new ArrayList<>();
    }

    public void setListaVariaveis(ArrayList<Variaveis> listaVariaveis) {
        this.listaVariaveis = listaVariaveis;
    }

    public Token getTipo() {
        return tipo;
    }

    public void setTipo(Token tipo) {
        this.tipo = tipo;
    }

    public Token getIde() {
        return ide;
    }

    public void setIde(Token ide) {
        this.ide = ide;
    }

    public ArrayList<Variaveis> getParametros() {
        return listaVariaveis;
    }

    public Token getRetorno() {
        return retorno;
    }

    public void setRetorno(Token retorno) {
        this.retorno = retorno;
    }

    public Token getRetornoAtribuido() {
        return retorno_atribuido;
    }

    public void setRetornoAtribuido(Token retorno_atribuido) {
        this.retorno_atribuido = retorno_atribuido;
    }

}
