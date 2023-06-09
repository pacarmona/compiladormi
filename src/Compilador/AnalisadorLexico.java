
package Compilador;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 *
 * @author Patricia Carmona
 */
public class AnalisadorLexico {

    private ArrayList<Token> listarTokens;
    private ArrayList<String> listarErros;
    private ArrayList<String> codigo;
    private static final char EOF = '\0';
    private int linha, aux;
    private boolean linhaVazia;
    private final EstruturaLexica token;

    public AnalisadorLexico() {
        this.listarTokens = new ArrayList<>();
        this.listarErros = new ArrayList<>();
        this.codigo = new ArrayList<>();
        this.linha = 0;
        this.aux = 0;
        this.linhaVazia = false;
        this.token = new EstruturaLexica();
    }

    void analisadorLexico(ArrayList<String> codigoFonte) {
        this.codigo = codigoFonte;
        char a = proximo();
        while (a != EOF) {
            verificaAutomato(a);
            a = proximo();
        }
    }

    private char proximo() {
        if (!codigo.isEmpty()) {

            char c[] = codigo.get(linha).toCharArray();

            if (c.length == aux) {
                linhaVazia = false;
                return ' ';
            }

            else if (c.length > aux) {
                linhaVazia = false;
                return c[aux];
            } else if (codigo.size() > (linha + 1)) {
                linha++;
                c = codigo.get(linha).toCharArray();
                aux = 0;
                if (c.length == 0) {
                    this.linhaVazia = true;
                    return ' ';
                }
                return c[aux];
            } else {
                return EOF;
            }
        } else {
            return EOF;
        }
    }

    private void verificaAutomato(char a) {
        String lexema = "";
        if (!this.linhaVazia) {

            if (token.verificarEspaco(a)) {
                aux++;
            } else if (token.verificarLetra(a)) {
                palavraReservadaId(lexema, a);
            } else if (Character.isDigit(a)) {
                numero(lexema, a);
            } else if (token.verificarOperador(a)) {
                operador(lexema, a);
            } else if (token.verificarDelimitador(a)) {
                delimitador(lexema, a);
            } else if (a == '/') {
                comentarioLinha(lexema, a);
            } else if (a == '"') {
                cadeiaDeCaractere(lexema, a);
            } else {
                this.palavraInvalida(lexema, a);
            }

        } else {
            linhaVazia = false;
            linha++;
        }
    }

    private void palavraReservadaId(String lexema, char a) {

        int linhaInicial = linha;
        int auxPalavraReservadaId = aux;
        Token tokenaux;

        lexema = lexema + a;
        this.aux++;
        a = this.proximo();

        while (a == '_' || Character.isLetterOrDigit(a)) {
            lexema = lexema + a;
            aux++;
            a = this.proximo();
        }
        tokenaux = token.verificarPalavrasReservada(lexema)
                ? lexema.equals("true") || lexema.equals("false")
                        ? new Token(linhaInicial + 1, auxPalavraReservadaId + 1, "boolean", lexema)
                        : new Token(linhaInicial + 1, auxPalavraReservadaId + 1, "PRE", lexema)
                : new Token(linhaInicial + 1, auxPalavraReservadaId + 1, "IDE", lexema);

        listarTokens.add(tokenaux);
    }

    private void palavraInvalida(String lexema, char a) {

        int linhaInicial = this.linha;
        lexema = lexema + a;
        this.aux++;
        this.addErro("SIB", lexema, linhaInicial);

    }

    private void delimitador(String lexema, char a) {

        int linhaInicial = this.linha;
        int auxiliarDelimitador = this.aux;
        Token tokenAuxiliar;

        lexema = lexema + a;
        this.aux++;
        tokenAuxiliar = new Token(linhaInicial + 1, auxiliarDelimitador + 1, "DEL", lexema);
        listarTokens.add(tokenAuxiliar);
    }

    private void operador(String lexema, char a) {

        if (a == '+' || a == '-' || a == '*') {
            operadorAritmetico(lexema, a);
        } else if (a == '<' || a == '>' || a == '=' || a == '!') {
            operadorRelacional(lexema, a);
        } else {
            operadorLogico(lexema, a);
        }
    }

    private void operadorAritmetico(String lexema, char a) {

        int linhaInicial = this.linha;
        int auxiliarOpAritmetico = this.aux;
        Token tokenAuxiliar;
        Token tokenAnterior;

        lexema = lexema + a;
        this.aux++;

        if (a == '+') {
            a = this.proximo();
            if (a == '+') {
                lexema = lexema + a;
                this.aux++;
            }
        } else if (a == '-') {
            a = this.proximo();
            tokenAnterior = listarTokens.get(listarTokens.size() - 1);
            if (Character.isSpaceChar(a)) {
                do {
                    this.aux++;
                    a = this.proximo();
                } while (token.verificarEspaco(a));
                if (Character.isDigit(a) && linhaInicial == linha) {
                    if (!(tokenAnterior.getTipo().equals("NRO") || tokenAnterior.getTipo().equals("IDE"))) {
                        this.numero(lexema, a);
                        return;
                    }
                }
            } else if (a == '-') {
                lexema = lexema + a;
                this.aux++;
            } else if (Character.isDigit(a)) {

                if (!(tokenAnterior.getTipo().equals("NRO") || tokenAnterior.getTipo().equals("IDE"))) {
                    this.numero(lexema, a);
                    return;
                }
            }

        }
        tokenAuxiliar = new Token(linhaInicial + 1, auxiliarOpAritmetico + 1, "ART", lexema);
        listarTokens.add(tokenAuxiliar);
    }

    private void comentarioLinha(String lexema, char a) {
        int linhaInicial = this.linha;
        int auxiliarComentario = this.aux;
        Token tokenAuxiliar;

        lexema = lexema + a;
        this.aux++;
        a = this.proximo();

        switch (a) {
            case '/':
                do {
                    lexema = lexema + a;
                    this.aux++;
                    a = this.proximo();
                } while (linha == linhaInicial && a != EOF);
                break;
            case '*':
                this.comentarioBloco(lexema, a, linhaInicial);
                return;
            default:
                tokenAuxiliar = new Token(linhaInicial + 1, auxiliarComentario + 1, "ART", lexema);
                this.listarTokens.add(tokenAuxiliar);
                break;
        }
    }

    private void comentarioBloco(String lexema, char a, int linhaInicialComent) {
        int linhaInicial = linhaInicialComent;

        do {
            lexema = lexema + a;
            this.aux++;
            a = this.proximo();
        } while (a != '*' && a != EOF);

        if (a == '*') {
            lexema = lexema + a;
            this.aux++;
            a = this.proximo();

            switch (a) {
                case '/':
                    lexema = lexema + a;
                    this.aux++;
                    break;
                default:
                    this.comentarioBloco(lexema, a, linhaInicial);
                    break;
            }
        } else {
            this.addErro("CoMF", lexema, linhaInicial);
        }

    }

    private void operadorRelacional(String lexema, char a) {

        int linhaInicial = this.linha;
        int auxiliarOperador = this.aux;
        Token tokenAuxiliar;

        lexema = lexema + a;
        this.aux++;

        if (a == '<' || a == '>' || a == '=') {
            a = this.proximo();
            if (a == '=') {
                lexema = lexema + a;
                this.aux++;
            }
            tokenAuxiliar = new Token(linhaInicial + 1, auxiliarOperador + 1, "REL", lexema);
            listarTokens.add(tokenAuxiliar);

        } else if (a == '!') {
            a = this.proximo();
            if (a == '=') {
                lexema = lexema + a;
                this.aux++;
                tokenAuxiliar = new Token(linhaInicial + 1, auxiliarOperador + 1, "REL", lexema);
                listarTokens.add(tokenAuxiliar);
            } else {
                tokenAuxiliar = new Token(linhaInicial + 1, auxiliarOperador + 1, "LOG", lexema);
                listarTokens.add(tokenAuxiliar);
            }
        }
    }

    private void operadorLogico(String lexema, char a) {

        int linhaInicial = this.linha;
        int auxiliarOperador = this.aux;
        Token tokenAuxiliar;

        lexema = lexema + a;
        this.aux++;

        if (a == '&') {
            a = this.proximo();
            if (a == '&') {
                lexema = lexema + a;
                this.aux++;
                tokenAuxiliar = new Token(linhaInicial + 1, auxiliarOperador + 1, "LOG", lexema);
                listarTokens.add(tokenAuxiliar);
            } else {
                this.addErro("LOGMF", lexema, linhaInicial);
            }
        } else if (a == '|') {
            a = this.proximo();
            if (a == '|') {
                lexema = lexema + a;
                this.aux++;
                tokenAuxiliar = new Token(linhaInicial + 1, auxiliarOperador + 1, "LOG", lexema);
                listarTokens.add(tokenAuxiliar);
            } else {
                this.addErro("LOGMF", lexema, linhaInicial);
            }
        }
    }

    private void numero(String lexema, char a) {

        int linhaInicial = linha;
        int auxiliarNumero = aux;
        Token tokenAuxiliar;
        boolean erro = false;

        do {
            lexema = lexema + a;
            this.aux++;
            a = this.proximo();
        } while (Character.isDigit(a));
        if (a == '.') {
            lexema = lexema + a;
            this.aux++;
            a = this.proximo();
            if (!Character.isDigit(a)) {
                erro = true;
            }
            while (Character.isDigit(a)) {
                lexema = lexema + a;
                this.aux++;
                a = this.proximo();
            }
            if (!erro) {
                tokenAuxiliar = new Token(linhaInicial + 1, auxiliarNumero + 1, "float", lexema);
                listarTokens.add(tokenAuxiliar);
                return;
            } else {
                addErro("NMF", lexema, linhaInicial);
            }
        }
        tokenAuxiliar = new Token(linhaInicial + 1, auxiliarNumero + 1, "int", lexema);
        listarTokens.add(tokenAuxiliar);

    }

    private void cadeiaDeCaractere(String lexema, char a) {

        int linhaInicial = this.linha;
        int auxiliarCadeiaCaractere = this.aux;
        Token tokenAuxiliar;

        lexema = lexema + a;
        this.aux++;
        a = this.proximo();
        while (a != '"' && linhaInicial == linha) {
            if (a == ((char) 92)) {
                this.aux++;
                lexema = lexema + a;
                a = this.proximo();
                if (a == '"') {
                    this.cadeiaDeCaractere(lexema, a);
                    return;
                }
            } else if (Character.isLetterOrDigit(a) || token.verificarSimbolo(a)) {
                lexema = lexema + a;
                this.aux++;
                a = this.proximo();
            } else {
                this.addErro("CMF", lexema, linhaInicial);
                return;
            }

        }
        if (a == '"' && linhaInicial == linha) {
            lexema = lexema + a;
            this.aux++;
            tokenAuxiliar = new Token(linhaInicial + 1, auxiliarCadeiaCaractere + 1, "string", lexema);
            this.listarTokens.add(tokenAuxiliar);
        } else {
            this.addErro("CMF", lexema, linhaInicial);
        }

    }

    private void addErro(String tipo, String erro, int linha) {
        NumberFormat formatter = new DecimalFormat("00");
        String s = formatter.format(linha + 1);
        listarErros.add(s + " " + tipo + " " + erro + " ");
    }

    // retorna a lista de erros
    public ArrayList<String> getListarErros() {
        return listarErros;
    }

    // retorna a lista de tokens válidos
    public ArrayList<Token> getListarTokens() {
        return listarTokens;
    }
}
