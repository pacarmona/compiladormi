package Compilador;

import java.util.ArrayList;

/**
 * 
 * @author Patricia Carmona
 */
public class EstruturaLexica {

    private final String[] reservadas = { "var", "const", "procedure", "function",
            "if", "else", "for", "read", "write", "int", "float", "boolean", "string", "true", "false" };
    private final ArrayList<String> palavrasReservadas = new ArrayList<>();

    private final char[] operadores = { '+', '-', '*', '<', '=', '>', '!', '&', '|' };
    private final ArrayList<Character> operador = new ArrayList<>();

    private final char[] delimitador = { '.', ';', ',', '(', ')', '[', ']', '{', '}' };
    private final ArrayList<Character> delimitadores = new ArrayList<>();

    private final ArrayList<Character> simbolos = new ArrayList<>();
    private final ArrayList<Character> letras = new ArrayList<>();

    public EstruturaLexica() {

        for (String palavra : reservadas) {
            palavrasReservadas.add(palavra);
        }

        for (char op : operadores) {
            operador.add(op);
        }

        for (char del : delimitador) {
            delimitadores.add(del);
        }

        for (char i = 'A'; i <= 'Z'; i++) {
            this.letras.add((char) i);
        }
        for (char i = 'a'; i <= 'z'; i++) {
            this.letras.add((char) i);
        }

        for (int i = 32; i <= 126; i++) {
            if (i != 34) {
                this.simbolos.add((char) i);
            }
        }

    }

    public boolean verificarPalavrasReservada(String string) {
        return this.palavrasReservadas.contains(string);
    }

    public boolean verificarOperador(char caractere) {
        return (this.operador.contains(caractere));
    }

    public boolean verificarDelimitador(char caractere) {
        return this.delimitadores.contains(caractere);
    }

    public boolean verificarLetra(char caractere) {
        return this.letras.contains(caractere);
    }

    public boolean verificarSimbolo(char caractere) {
        return this.simbolos.contains(caractere);
    }

    public boolean verificarEspaco(char caractere) {
        return (Character.isSpaceChar(caractere) || caractere == 9);
    }
}
