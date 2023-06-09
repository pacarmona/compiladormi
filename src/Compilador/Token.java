package Compilador;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author Patricia Carmona
 */
public class Token {

    private String tipo;
    private final int linha;
    private final int aux;
    private final String lexema;

    public Token(int linha, int aux, String tipo, String lexema) {

        this.tipo = tipo;
        this.linha = linha;
        this.aux = aux;
        this.lexema = lexema;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getLinha() {
        NumberFormat formatter = new DecimalFormat("00");
        String s = formatter.format(linha);

        return s;
    }

    public int getAux() {
        return aux;
    }

    public String getLexema() {
        return lexema;
    }

}
