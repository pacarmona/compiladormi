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
public class ChamadaFuncao {

    private Token ide;
    private ArrayList<Token> parChamada;

    public ChamadaFuncao() {
        this.parChamada = new ArrayList<Token>();
    }

    public Token getIde() {
        return ide;
    }

    public void setIde(Token ide) {
        this.ide = ide;
    }

    public void setParChamada(ArrayList<Token> parChamada) {
        this.parChamada = parChamada;
    }

    public ArrayList<Token> getParChamada() {
        return parChamada;
    }

}
