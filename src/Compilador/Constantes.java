/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Compilador;

/**
 *
 * @author Patricia Carmona
 */
public class Constantes {
    private Token ide;
    private Token tipo;
    private Token valor;

    public Constantes() {

    }

    public Token getIde() {
        return ide;
    }

    public void setIde(Token ide) {
        this.ide = ide;
    }

    public Token getTipo() {
        return tipo;
    }

    public void setTipo(Token tipo) {
        this.tipo = tipo;
    }

    public Token getValor() {
        return valor;
    }

    public void setValor(Token valor) {
        this.valor = valor;
    }
}
