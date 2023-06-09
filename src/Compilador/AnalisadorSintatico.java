package Compilador;

import java.util.ArrayList;

/**
 *
 * @author Patricia Carmona
 */
public class AnalisadorSintatico {

    private ArrayList<String> listarErros;
    private ArrayList<String> tipos;
    private ArrayList<String> opRel;

    private final String[] tipoauxiliar = { "int", "float", "boolean", "string", "proc" };
    private final String[] opRelauxiliar = { ">", "<", ">=", "<=", "!=", "==" };

    private ArrayList<Token> listarTokens;
    private int posicaoAtual;
    private int posicaoFinal;
    private Token ultimo, penultimo, aux_tipo, aux_tipo_2, auxvar;

    private Tabela tabela = new Tabela();
    int a;

    public AnalisadorSintatico() {
        listarErros = new ArrayList<>();
        opRel = new ArrayList<>();
        posicaoAtual = 0;
        tipos = new ArrayList<>();

        for (String tipoaux : tipoauxiliar) {
            tipos.add(tipoaux);
        }
        for (String opRelaux : opRelauxiliar) {
            opRel.add(opRelaux);
        }

    }

    // testado
    public void analiseSintatica(ArrayList<Token> tokens) {
        listarTokens = tokens;
        penultimo = listarTokens.get(listarTokens.size() - 1);
        ultimo = new Token(listarTokens.size(), penultimo.getAux(), "$", "$");
        listarTokens.add(ultimo);
        posicaoFinal = listarTokens.size();

        programa();
    }

    // pega o token atual --------- testado
    public Token atual() {
        if (posicaoAtual < posicaoFinal) {
            return (Token) listarTokens.get(posicaoAtual);
        }
        return null;
    }

    public Token aa() {
        if (a < posicaoFinal) {
            return (Token) listarTokens.get(a);
        }
        return null;

    }

    // pega o proximo token ------- testado
    public Token seguinte() {
        if (posicaoAtual < posicaoFinal) {
            if (listarTokens.get(posicaoAtual + 1) != null) {
                return (Token) listarTokens.get(posicaoAtual + 1);
            }
        }
        return null;
    }

    // pega o token de sincronização ----------- testado
    public void sincronizacao(String sinc) {

        while (!(atual().getLexema().equals(sinc))) {
            posicaoAtual = posicaoAtual + 1;
        }

    }

    // testado
    public ArrayList<String> getListarErros() {
        return listarErros;
    }

    public ArrayList<String> getListarErrosSem() {
        // System.out.println(tabela.getErros());
        return tabela.getErros();
    }

    // testado
    public void addErro(Token token, String erro) {
        listarErros.add(
                "Linha: " + token.getLinha() + " Recebido: " + "'" + token.getLexema() + "'" + " Esperado: " + erro);
    }

    // <Programa> ::= <Constantes> <Variaveis> <Funcoes>
    public void programa() {
        Constantes();
        Variaveis();
        Funcoes();
        tabela.iniciarSemantico();

    }

    // <Constantes> ::= const '{' Tipo Identificador '=' <Valores> <MaisConstantes>
    // '}' | <>
    public void Constantes() {
        if ((atual() != null) && (atual().getLexema().equals("const"))) {
            posicaoAtual = posicaoAtual + 1;

            tabela.addconst();

            if ((atual() != null) && (atual().getLexema().equals("{"))) {
                posicaoAtual = posicaoAtual + 1;
                if ((atual() != null) && (tipos.contains(atual().getLexema()))) {

                    tabela.setTipoconst(atual());

                    posicaoAtual = posicaoAtual + 1;
                    if ((atual() != null) && (atual().getTipo().equals("IDE"))) {

                        tabela.setIdeconst(atual());

                        posicaoAtual = posicaoAtual + 1;
                        if ((atual() != null) && (atual().getLexema().equals("="))) {
                            posicaoAtual = posicaoAtual + 1;

                            tabela.setValorconst(atual());

                            valores();

                            tabela.addListaconst();

                            MaisConstantes();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "'}'");
                                System.out.println(atual().getLinha());
                            }
                        } else {
                            addErro(atual(), "'='");
                            if (seguinte().getLexema().equals("=")) {
                                posicaoAtual = posicaoAtual + 1;
                                valores();
                                MaisConstantes();
                            } else {
                                while (!(atual().getLexema().equals(";")
                                        || atual().getLexema().equals(",")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getTipo().equals("string")
                                        || atual().getTipo().equals("int")
                                        || atual().getTipo().equals("float")
                                        || atual().getLexema().equals("false")
                                        || atual().getLexema().equals("true")
                                        || atual().getLexema().equals("$")
                                        || tipos.contains(atual().getLexema())
                                        || atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                switch (atual().getLexema()) {
                                    case "$":
                                        addErro(atual(), "Fim de programa");
                                        break;
                                    case "}":
                                        addErro(atual(), "valores e ';'");
                                        break;
                                    case ";":
                                    case ",":
                                        MaisConstantes();
                                        break;
                                    default:
                                        valores();
                                        MaisConstantes();
                                        break;
                                }
                            }
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "'}'");
                            }
                        }

                    } else {
                        addErro(atual(), "IDE");
                        if (seguinte().getTipo().equals("IDE")) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            while (!(atual().getLexema().equals("=")
                                    || atual().getLexema().equals("$")
                                    || atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "Fim de programa");
                            }
                        }
                        if ((atual() != null) && (atual().getLexema().equals("="))) {
                            posicaoAtual = posicaoAtual + 1;
                            valores();
                            MaisConstantes();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "'}'");
                            }
                        } else {
                            addErro(atual(), "'='");
                            if (seguinte().getLexema().equals("=")) {
                                posicaoAtual = posicaoAtual + 1;
                                valores();
                                MaisConstantes();
                            } else {
                                while (!(atual().getLexema().equals(";")
                                        || atual().getLexema().equals(",")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getTipo().equals("string")
                                        || atual().getTipo().equals("int")
                                        || atual().getTipo().equals("float")
                                        || atual().getLexema().equals("false")
                                        || atual().getLexema().equals("true")
                                        || atual().getLexema().equals("$")
                                        || tipos.contains(atual().getLexema())
                                        || atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                switch (atual().getLexema()) {
                                    case "$":
                                        addErro(atual(), "Fim de programa");
                                        break;
                                    case "}":
                                        addErro(atual(), "valores e ';'");
                                        break;
                                    case ";":
                                    case ",":
                                        MaisConstantes();
                                        break;
                                    default:
                                        valores();
                                        MaisConstantes();
                                        break;
                                }

                            }
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "'}'");
                            }
                        }
                    }

                } else {
                    addErro(atual(), "tipo");
                    while (!(atual().getTipo().equals("IDE")
                            || atual().getLexema().equals(",")
                            || atual().getLexema().equals(";")
                            || atual().getLexema().equals("}")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "Fim de programa");
                    } else if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                        posicaoAtual = posicaoAtual + 1;
                        if ((atual() != null) && (atual().getLexema().equals("="))) {
                            posicaoAtual = posicaoAtual + 1;
                            valores();
                            MaisConstantes();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "'}'");
                            }
                        } else {
                            addErro(atual(), "'='");
                            if (seguinte().getLexema().equals("=")) {
                                posicaoAtual = posicaoAtual + 1;
                                valores();
                                MaisConstantes();
                            } else {
                                while (!(atual().getLexema().equals(";")
                                        || atual().getLexema().equals(",")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getTipo().equals("string")
                                        || atual().getTipo().equals("int")
                                        || atual().getTipo().equals("float")
                                        || atual().getLexema().equals("false")
                                        || atual().getLexema().equals("true")
                                        || atual().getLexema().equals("$")
                                        || tipos.contains(atual().getLexema())
                                        || atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                switch (atual().getLexema()) {
                                    case "$":
                                        addErro(atual(), "Fim de programa");
                                        break;
                                    case "}":
                                        addErro(atual(), "valores e ';'");
                                        break;
                                    case ";":
                                    case ",":
                                        MaisConstantes();
                                        break;
                                    default:
                                        valores();
                                        MaisConstantes();
                                        break;
                                }
                            }
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "'}'");
                            }
                        }

                    } else {
                        addErro(atual(), "IDE");
                        if (seguinte().getTipo().equals("IDE")) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            while (!(atual().getLexema().equals("=")
                                    || atual().getLexema().equals("$")
                                    || atual().getLexema().equals(";")
                                    || atual().getLexema().equals(",")
                                    || atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "Fim de programa");
                            }
                        }
                        if ((atual() != null) && (atual().getLexema().equals("="))) {
                            posicaoAtual = posicaoAtual + 1;
                            valores();
                            MaisConstantes();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "'}'");
                            }
                        } else {
                            addErro(atual(), "'='");
                            if (seguinte().getLexema().equals("=")) {
                                posicaoAtual = posicaoAtual + 1;
                                valores();
                                MaisConstantes();
                            } else {
                                while (!(atual().getLexema().equals(";")
                                        || atual().getLexema().equals(",")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getTipo().equals("string")
                                        || atual().getTipo().equals("int")
                                        || atual().getTipo().equals("float")
                                        || atual().getLexema().equals("false")
                                        || atual().getLexema().equals("true")
                                        || atual().getLexema().equals("$")
                                        || tipos.contains(atual().getLexema())
                                        || atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                switch (atual().getLexema()) {
                                    case "$":
                                        addErro(atual(), "Fim de programa");
                                        break;
                                    case "}":
                                        addErro(atual(), "valores e ';'");
                                        break;
                                    case ";":
                                    case ",":
                                        MaisConstantes();
                                        break;
                                    default:
                                        valores();
                                        MaisConstantes();
                                        break;
                                }
                            }
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "'}'");
                            }
                        }
                    }
                }

            } else {
                addErro(atual(), "'{'");
                if (seguinte().getLexema().equals("{")) {
                    posicaoAtual = posicaoAtual + 1;
                } else {
                    while (!(tipos.contains(atual().getLexema())
                            || atual().getTipo().equals("IDE")
                            || atual().getLexema().equals("}")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "Fim de programa");
                    }
                }
                if ((atual() != null) && (tipos.contains(atual().getLexema()))) {
                    posicaoAtual = posicaoAtual + 1;
                    if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                        posicaoAtual = posicaoAtual + 1;
                        if ((atual() != null) && (atual().getLexema().equals("="))) {
                            posicaoAtual = posicaoAtual + 1;
                            valores();
                            MaisConstantes();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "'}'");
                            }
                        } else {
                            addErro(atual(), "'='");
                            if (seguinte().getLexema().equals("=")) {
                                posicaoAtual = posicaoAtual + 1;
                                valores();
                                MaisConstantes();
                            } else {
                                while (!(atual().getLexema().equals(";")
                                        || atual().getLexema().equals(",")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getTipo().equals("string")
                                        || atual().getTipo().equals("int")
                                        || atual().getTipo().equals("float")
                                        || atual().getLexema().equals("false")
                                        || atual().getLexema().equals("true")
                                        || atual().getLexema().equals("$")
                                        || tipos.contains(atual().getLexema())
                                        || atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                switch (atual().getLexema()) {
                                    case "$":
                                        addErro(atual(), "Fim de programa");
                                        break;
                                    case "}":
                                        addErro(atual(), "valores e ';'");
                                        break;
                                    case ";":
                                    case ",":
                                        MaisConstantes();
                                        break;
                                    default:
                                        valores();
                                        MaisConstantes();
                                        break;
                                }
                            }
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "'}'");
                            }
                        }

                    } else {
                        addErro(atual(), "IDE");
                        if (seguinte().getTipo().equals("IDE")) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            while (!(atual().getLexema().equals("=")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("$")
                                    || atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "Fim de programa");
                            }
                        }
                        if ((atual() != null) && (atual().getLexema().equals("="))) {
                            posicaoAtual = posicaoAtual + 1;
                            valores();
                            MaisConstantes();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "'}'");
                            }
                        } else {
                            addErro(atual(), "'='");
                            if (seguinte().getLexema().equals("=")) {
                                posicaoAtual = posicaoAtual + 1;
                                valores();
                                MaisConstantes();
                            } else {
                                while (!(atual().getLexema().equals(";")
                                        || atual().getLexema().equals(",")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getTipo().equals("string")
                                        || atual().getTipo().equals("int")
                                        || atual().getTipo().equals("float")
                                        || atual().getLexema().equals("false")
                                        || atual().getLexema().equals("true")
                                        || atual().getLexema().equals("$")
                                        || tipos.contains(atual().getLexema())
                                        || atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                switch (atual().getLexema()) {
                                    case "$":
                                        addErro(atual(), "Fim de programa");
                                        break;
                                    case "}":
                                        addErro(atual(), "valores e ';'");
                                        break;
                                    case ";":
                                    case ",":
                                        MaisConstantes();
                                        break;
                                    default:
                                        valores();
                                        MaisConstantes();
                                        break;
                                }
                            }
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "'}'");
                            }
                        }
                    }

                } else {
                    addErro(atual(), "tipo");
                    while (!(atual().getTipo().equals("IDE")
                            || atual().getLexema().equals(",")
                            || atual().getLexema().equals(";")
                            || atual().getLexema().equals("}")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "Fim de programa");
                    } else if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                        posicaoAtual = posicaoAtual + 1;
                        if ((atual() != null) && (atual().getLexema().equals("="))) {
                            posicaoAtual = posicaoAtual + 1;
                            valores();
                            MaisConstantes();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "'}'");
                            }
                        } else {
                            addErro(atual(), "'='");
                            if (seguinte().getLexema().equals("=")) {
                                posicaoAtual = posicaoAtual + 1;
                                valores();
                                MaisConstantes();
                            } else {
                                while (!(atual().getLexema().equals(";")
                                        || atual().getLexema().equals(",")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getTipo().equals("string")
                                        || atual().getTipo().equals("int")
                                        || atual().getTipo().equals("float")
                                        || atual().getLexema().equals("false")
                                        || atual().getLexema().equals("true")
                                        || atual().getLexema().equals("$")
                                        || tipos.contains(atual().getLexema())
                                        || atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                switch (atual().getLexema()) {
                                    case "$":
                                        addErro(atual(), "Fim de programa");
                                        break;
                                    case "}":
                                        addErro(atual(), "valores e ';'");
                                        break;
                                    case ";":
                                    case ",":
                                        MaisConstantes();
                                        break;
                                    default:
                                        valores();
                                        MaisConstantes();
                                        break;
                                }
                            }
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "'}'");
                            }
                        }

                    } else {
                        addErro(atual(), "IDE");
                        if (seguinte().getTipo().equals("IDE")) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            while (!(atual().getLexema().equals("=")
                                    || atual().getLexema().equals("$")
                                    || atual().getLexema().equals("}")
                                    || atual().getLexema().equals(",")
                                    || atual().getLexema().equals(";"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "Fim de programa");
                            }
                        }
                        if ((atual() != null) && (atual().getLexema().equals("="))) {
                            posicaoAtual = posicaoAtual + 1;
                            valores();
                            MaisConstantes();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "'}'");
                            }
                        } else {
                            addErro(atual(), "'='");
                            if (seguinte().getLexema().equals("=")) {
                                posicaoAtual = posicaoAtual + 1;
                                valores();
                                MaisConstantes();
                            } else {
                                while (!(atual().getLexema().equals(";")
                                        || atual().getLexema().equals(",")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getTipo().equals("string")
                                        || atual().getTipo().equals("int")
                                        || atual().getTipo().equals("float")
                                        || atual().getLexema().equals("false")
                                        || atual().getLexema().equals("true")
                                        || atual().getLexema().equals("$")
                                        || tipos.contains(atual().getLexema())
                                        || atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                switch (atual().getLexema()) {
                                    case "$":
                                        addErro(atual(), "Fim de programa");
                                        break;
                                    case "}":
                                        addErro(atual(), "valores e ';'");
                                        break;
                                    case ";":
                                    case ",":
                                        MaisConstantes();
                                        break;
                                    default:
                                        valores();
                                        MaisConstantes();
                                        break;
                                }
                            }
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "'}'");
                            }
                        }
                    }
                }
            }

        }

    }
    // <MaisConstantes> ::= ',' Identificador '=' <Valores> <MaisConstantes> | ';'
    // <OutrasConstantes>

    public void MaisConstantes() {
        if ((atual() != null) && (atual().getLexema().equals(","))) {
            posicaoAtual = posicaoAtual + 1;

            tabela.addconst();
            a = posicaoAtual;
            while (!(tipos.contains(aa().getLexema()))) {
                a = a - 1;
            }
            tabela.setTipoconst(aa());

            if ((atual() != null) && (atual().getTipo().equals("IDE"))) {

                tabela.setIdeconst(atual());

                posicaoAtual = posicaoAtual + 1;
                if ((atual() != null) && (atual().getLexema().equals("="))) {
                    posicaoAtual = posicaoAtual + 1;

                    tabela.setValorconst(atual());

                    valores();

                    tabela.addListaconst();

                    MaisConstantes();
                } else {
                    addErro(atual(), "'='");
                    if (seguinte().getLexema().equals("=")) {
                        posicaoAtual = posicaoAtual + 1;
                        valores();
                        MaisConstantes();
                    } else {
                        while (!(atual().getLexema().equals(";")
                                || atual().getLexema().equals(",")
                                || atual().getTipo().equals("IDE")
                                || atual().getTipo().equals("string")
                                || atual().getTipo().equals("int")
                                || atual().getTipo().equals("float")
                                || atual().getLexema().equals("false")
                                || atual().getLexema().equals("true")
                                || atual().getLexema().equals("$")
                                || tipos.contains(atual().getLexema())
                                || atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        switch (atual().getLexema()) {
                            case "$":
                                addErro(atual(), "Fim de programa");
                                break;
                            case "}":
                                addErro(atual(), "valores e ';'");
                                break;
                            case ";":
                            case ",":
                                MaisConstantes();
                                break;
                            default:
                                valores();
                                MaisConstantes();
                                break;
                        }
                    }
                }
            } else {
                addErro(atual(), "IDE");
                if (seguinte().getTipo().equals("IDE")) {
                    posicaoAtual = posicaoAtual + 1;
                } else {
                    while (!(atual().getLexema().equals("=")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "fim de programa");
                    }
                }
                if ((atual() != null) && (atual().getLexema().equals("="))) {
                    posicaoAtual = posicaoAtual + 1;
                    valores();
                    MaisConstantes();
                } else {
                    addErro(atual(), "'='");
                    if (seguinte().getLexema().equals("=")) {
                        posicaoAtual = posicaoAtual + 1;
                        valores();
                        MaisConstantes();
                    } else {
                        while (!(atual().getLexema().equals(";")
                                || atual().getLexema().equals(",")
                                || atual().getTipo().equals("IDE")
                                || atual().getTipo().equals("string")
                                || atual().getTipo().equals("int")
                                || atual().getTipo().equals("float")
                                || atual().getLexema().equals("false")
                                || atual().getLexema().equals("true")
                                || atual().getLexema().equals("$")
                                || tipos.contains(atual().getLexema())
                                || atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        switch (atual().getLexema()) {
                            case "$":
                                addErro(atual(), "Fim de programa");
                                break;
                            case "}":
                                addErro(atual(), "valores e ';'");
                                break;
                            case ";":
                            case ",":
                                MaisConstantes();
                                break;
                            default:
                                valores();
                                MaisConstantes();
                                break;
                        }
                    }
                }
            }
        } else if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
            addErro(atual(), "','");
            posicaoAtual = posicaoAtual + 1;
            if ((atual() != null) && (atual().getLexema().equals("="))) {
                posicaoAtual = posicaoAtual + 1;
                valores();
                MaisConstantes();
            } else {
                addErro(atual(), "'='");
                if (seguinte().getLexema().equals("=")) {
                    posicaoAtual = posicaoAtual + 1;
                    valores();
                    MaisConstantes();
                } else {
                    while (!(atual().getLexema().equals(";")
                            || atual().getLexema().equals(",")
                            || atual().getTipo().equals("IDE")
                            || atual().getTipo().equals("string")
                            || atual().getTipo().equals("int")
                            || atual().getTipo().equals("float")
                            || atual().getLexema().equals("false")
                            || atual().getLexema().equals("true")
                            || atual().getLexema().equals("$")
                            || tipos.contains(atual().getLexema())
                            || atual().getLexema().equals("}"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    switch (atual().getLexema()) {
                        case "$":
                            addErro(atual(), "Fim de programa");
                            break;
                        case "}":
                            addErro(atual(), "valores e ';'");
                            break;
                        case ";":
                        case ",":
                            MaisConstantes();
                            break;
                        default:
                            valores();
                            MaisConstantes();
                            break;
                    }
                }
            }

        } else if ((atual() != null) && (atual().getLexema().equals(";"))) {
            posicaoAtual = posicaoAtual + 1;
            if (!(atual().getLexema().equals("$") || atual().getLexema().equals("}"))) {
                OutrasConstantes();
            }
        } else {
            if (tipos.contains(atual().getLexema())) {
                addErro(atual(), "';'");
                OutrasConstantes();
            } else if (atual().getTipo().equals("IDE")) {
                MaisConstantes();
            } else if (atual().getLexema().equals("}")) {
                addErro(atual(), "';'");
            } else {
                addErro(atual(), "Não reconhece para declaração de constante");
                while (!(tipos.contains(atual().getLexema())
                        || atual().getLexema().equals(";")
                        || atual().getLexema().equals(",")
                        || atual().getLexema().equals("}")
                        || atual().getLexema().equals("$"))) {
                    posicaoAtual = posicaoAtual + 1;
                }
                if (atual().getLexema().equals("$")) {
                    addErro(atual(), "fim de programa");
                } else if (atual().getLexema().equals("}")) {
                    return;
                } else if (tipos.contains(atual().getLexema())) {
                    OutrasConstantes();
                }
                MaisConstantes();
            }
        }

    }
    // <OutrasConstantes> ::= Tipo Identificador '=' <Valores> <MaisConstantes> | <>

    public void OutrasConstantes() {

        tabela.addconst();

        if ((atual() != null) && (tipos.contains(atual().getLexema()))) {

            tabela.setTipoconst(atual());

            posicaoAtual = posicaoAtual + 1;
            if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                tabela.setIdeconst(atual());

                posicaoAtual = posicaoAtual + 1;
                if ((atual() != null) && (atual().getLexema().equals("="))) {
                    posicaoAtual = posicaoAtual + 1;

                    tabela.setValorconst(atual());

                    valores();

                    tabela.addListaconst();

                    MaisConstantes();
                } else {
                    addErro(atual(), "'='");
                    if (seguinte().getLexema().equals("=")) {
                        posicaoAtual = posicaoAtual + 1;
                        valores();
                        MaisConstantes();
                    } else {
                        while (!(atual().getLexema().equals(";")
                                || atual().getLexema().equals(",")
                                || atual().getTipo().equals("IDE")
                                || atual().getTipo().equals("string")
                                || atual().getTipo().equals("int")
                                || atual().getTipo().equals("float")
                                || atual().getLexema().equals("false")
                                || atual().getLexema().equals("true")
                                || atual().getLexema().equals("$")
                                || tipos.contains(atual().getLexema())
                                || atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        switch (atual().getLexema()) {
                            case "$":
                                addErro(atual(), "Fim de programa");
                                break;
                            case "}":
                                addErro(atual(), "valores e ';'");
                                break;
                            case ";":
                            case ",":
                                MaisConstantes();
                                break;
                            default:
                                valores();
                                MaisConstantes();
                                break;
                        }
                    }
                }
            } else {
                addErro(atual(), "IDE");
                if (seguinte().getTipo().equals("IDE")) {
                    posicaoAtual = posicaoAtual + 1;
                } else {
                    while (!(atual().getLexema().equals("=")
                            || tipos.contains(atual().getLexema())
                            || atual().getTipo().equals("IDE")
                            || atual().getTipo().equals("string")
                            || atual().getTipo().equals("int")
                            || atual().getTipo().equals("float")
                            || atual().getLexema().equals("false")
                            || atual().getLexema().equals("true")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "fim de programa");
                    }
                }
                if ((atual() != null) && (atual().getLexema().equals("="))) {
                    posicaoAtual = posicaoAtual + 1;
                    valores();
                    MaisConstantes();
                } else {
                    addErro(atual(), "'='");
                    if (seguinte().getLexema().equals("=")) {
                        posicaoAtual = posicaoAtual + 1;
                        valores();
                        MaisConstantes();
                    } else {
                        while (!(atual().getLexema().equals(";")
                                || atual().getLexema().equals(",")
                                || atual().getTipo().equals("IDE")
                                || atual().getTipo().equals("string")
                                || atual().getTipo().equals("int")
                                || atual().getTipo().equals("float")
                                || atual().getLexema().equals("false")
                                || atual().getLexema().equals("true")
                                || atual().getLexema().equals("$")
                                || tipos.contains(atual().getLexema())
                                || atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        switch (atual().getLexema()) {
                            case "$":
                                addErro(atual(), "Fim de programa");
                                break;
                            case "}":
                                addErro(atual(), "valores e ';'");
                                break;
                            case ";":
                            case ",":
                                MaisConstantes();
                                break;
                            default:
                                valores();
                                MaisConstantes();
                                break;
                        }
                    }
                }
            }
        }
    }

    // <Valores> ::= Cadeia | Identificador <Matriz> | int | float | 'true' |
    // 'false'
    public void valores() {
        if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
            posicaoAtual = posicaoAtual + 1;
            if (atual().getLexema().equals("[")) {
                Matriz();
            }
        } else if ((atual() != null) && (atual().getTipo().equals("string")
                || atual().getTipo().equals("int") || atual().getTipo().equals("float")
                || atual().getLexema().equals("true") || atual().getLexema().equals("false"))) {
            posicaoAtual = posicaoAtual + 1;
        } else {
            addErro(atual(), "Valor incorreto para valores");
            while (!(atual().getLexema().equals(",")
                    || atual().getLexema().equals(";")
                    || atual().getLexema().equals("$")
                    || atual().getLexema().equals("}"))) {
                posicaoAtual = posicaoAtual + 1;
            }
            if (atual().getLexema().equals("$")) {
                addErro(atual(), "fim de programa");
            }
        }

    }
    // <Matriz> ::= '[' <MatrizId> ']' | '[' <MatrizId> ']' '[' <MatrizId> ']' | <>

    public void Matriz() {
        if ((atual() != null) && (atual().getLexema().equals("["))) {
            posicaoAtual = posicaoAtual + 1;
            MatrizId();
            if ((atual() != null) && (atual().getLexema().equals("]"))) {
                posicaoAtual = posicaoAtual + 1;
                if ((atual() != null) && (atual().getLexema().equals("["))) {
                    posicaoAtual = posicaoAtual + 1;
                    MatrizId();
                    if ((atual() != null) && (atual().getLexema().equals("]"))) {
                        posicaoAtual = posicaoAtual + 1;
                    } else {
                        addErro(atual(), "']'");
                        if (seguinte().getLexema().equals("]")) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            while (!(atual().getLexema().equals("]")
                                    || atual().getLexema().equals(";")
                                    || atual().getLexema().equals(",")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            }
                        }
                    }
                }
            } else {
                addErro(atual(), "']'");
                if (seguinte().getLexema().equals("]")) {
                    posicaoAtual = posicaoAtual + 1;
                } else {
                    while (!(atual().getLexema().equals("[")
                            || atual().getLexema().equals(";")
                            || atual().getLexema().equals(",")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "fim de programa");
                    }
                }
                if ((atual() != null) && (atual().getLexema().equals("["))) {
                    posicaoAtual = posicaoAtual + 1;
                    MatrizId();
                    if ((atual() != null) && (atual().getLexema().equals("]"))) {
                        posicaoAtual = posicaoAtual + 1;
                    } else {
                        addErro(atual(), "']'");
                        if (seguinte().getLexema().equals("]")) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            while (!(atual().getLexema().equals("]")
                                    || atual().getLexema().equals(";")
                                    || atual().getLexema().equals(",")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            }
                        }
                    }
                }
            }
        }
    }
    // <MatrizId> ::= Identificador | int

    public void MatrizId() {
        if (atual().getTipo().equals("IDE")) {
            tabela.verificaInteiro(atual());
            posicaoAtual = posicaoAtual + 1;
        } else if (atual().getTipo().equals("int")) {
            posicaoAtual = posicaoAtual + 1;
        } else {
            addErro(atual(), "Valor incorreto para índice de matriz");
            if (seguinte().getLexema().equals("]")) {
                posicaoAtual = posicaoAtual + 1;
            } else {
                while (!(atual().getLexema().equals("]")
                        || atual().getLexema().equals(";")
                        || atual().getLexema().equals(",")
                        || atual().getLexema().equals("$"))) {
                    posicaoAtual = posicaoAtual + 1;
                }
                if (atual().getLexema().equals("$")) {
                    addErro(atual(), "fim de programa");
                }
            }

        }
    }

    // <Variaveis> ::= var '{' Tipo Identificador <Matriz> <MaisVariaveis> '}' | <>
    public void Variaveis() {
        if ((atual() != null) && (atual().getLexema().equals("var"))) {
            posicaoAtual = posicaoAtual + 1;
            if ((atual() != null) && (atual().getLexema().equals("{"))) {
                posicaoAtual = posicaoAtual + 1;

                tabela.addvar();

                if ((atual() != null) && (tipos.contains(atual().getLexema()))) {

                    tabela.setTipovar(atual());

                    posicaoAtual = posicaoAtual + 1;
                    if ((atual() != null) && (atual().getTipo().equals("IDE"))) {

                        tabela.setIdevar(atual());
                        tabela.addListavar();

                        posicaoAtual = posicaoAtual + 1;
                        Matriz();
                        MaisVariaveis();
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            addErro(atual(), "'}'");
                        }
                    } else {
                        addErro(atual(), "IDE");
                        Matriz();
                        if (seguinte().getTipo().equals("IDE")) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            while (!(atual().getLexema().equals(",")
                                    || atual().getLexema().equals(";")
                                    || atual().getLexema().equals("}")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "Fim de programa");
                            }
                        }
                        MaisVariaveis();
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            addErro(atual(), "'}'");
                        }
                    }
                    // } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                    // posicaoAtual = posicaoAtual + 1;
                } else {
                    addErro(atual(), "tipo");
                    if (tipos.contains(seguinte().getLexema())) {
                        posicaoAtual = posicaoAtual + 1;
                    } else {
                        while (!(atual().getTipo().equals("IDE")
                                || atual().getLexema().equals(",")
                                || atual().getLexema().equals(";")
                                || atual().getLexema().equals("}")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if (atual().getLexema().equals("$")) {
                            addErro(atual(), "Fim de programa");
                        }
                    }
                    if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                        posicaoAtual = posicaoAtual + 1;
                        Matriz();
                        MaisVariaveis();
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            addErro(atual(), "'}'");
                        }
                    } else {
                        addErro(atual(), "IDE");
                        Matriz();
                        if (seguinte().getTipo().equals("IDE")) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            while (!(atual().getLexema().equals(",")
                                    || atual().getLexema().equals(";")
                                    || atual().getLexema().equals("}")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "Fim de programa");
                            }
                        }
                        MaisVariaveis();
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            addErro(atual(), "'}'");
                        }
                    }
                }
            } else {
                addErro(atual(), "'{'");
                if (seguinte().getLexema().equals("{")) {
                    posicaoAtual = posicaoAtual + 1;
                } else {

                    while (!(atual().getTipo().equals("IDE")
                            || tipos.contains(atual().getLexema())
                            || atual().getLexema().equals(",")
                            || atual().getLexema().equals(";")
                            || atual().getLexema().equals("}")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "Fim de programa");
                    }

                }
                if ((atual() != null) && (tipos.contains(atual().getLexema()))) {
                    posicaoAtual = posicaoAtual + 1;
                    if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                        posicaoAtual = posicaoAtual + 1;
                        Matriz();
                        MaisVariaveis();
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            addErro(atual(), "'}'");
                        }
                    } else {
                        addErro(atual(), "IDE");
                        Matriz();
                        if (seguinte().getTipo().equals("IDE")) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            while (!(atual().getLexema().equals(",")
                                    || atual().getLexema().equals(";")
                                    || atual().getLexema().equals("}")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "Fim de programa");
                            }
                        }
                        MaisVariaveis();
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            addErro(atual(), "'}'");
                        }
                    }
                } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                    posicaoAtual = posicaoAtual + 1;
                } else {
                    addErro(atual(), "tipo");
                    if (tipos.contains(seguinte().getLexema())) {
                        posicaoAtual = posicaoAtual + 1;
                    } else {
                        while (!(atual().getTipo().equals("IDE")
                                || atual().getLexema().equals(",")
                                || atual().getLexema().equals(";")
                                || atual().getLexema().equals("}")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if (atual().getLexema().equals("$")) {
                            addErro(atual(), "Fim de programa");
                        }
                    }
                    if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                        posicaoAtual = posicaoAtual + 1;
                        Matriz();
                        MaisVariaveis();
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            addErro(atual(), "'}'");
                        }
                    } else {
                        addErro(atual(), "IDE");
                        Matriz();
                        if (seguinte().getTipo().equals("IDE")) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            while (!(atual().getLexema().equals(",")
                                    || atual().getLexema().equals(";")
                                    || atual().getLexema().equals("}")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "Fim de programa");
                            }
                        }
                        MaisVariaveis();
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            addErro(atual(), "'}'");
                        }
                    }
                }
            }
        }
    }
    // <MaisVariaveis> ::= ',' Identificador <Matriz> <MaisVariaveis> | ';'
    // <OutrasVariaveis>

    public void MaisVariaveis() {
        if ((atual() != null) && (atual().getLexema().equals(","))) {
            posicaoAtual = posicaoAtual + 1;

            tabela.addvar();
            a = posicaoAtual;
            while (!(tipos.contains(aa().getLexema()))) {
                a = a - 1;
            }
            tabela.setTipovar(aa());

            if ((atual() != null) && (atual().getTipo().equals("IDE"))) {

                tabela.setIdevar(atual());
                tabela.addListavar();

                posicaoAtual = posicaoAtual + 1;
                Matriz();
                MaisVariaveis();
            } else {
                addErro(atual(), "IDE");
                while (!(atual().getLexema().equals(",")
                        || atual().getLexema().equals(";")
                        || atual().getLexema().equals("$"))) {
                    posicaoAtual = posicaoAtual + 1;
                }
                if (atual().getLexema().equals("$")) {
                    addErro(atual(), "fim de programa");
                } else {
                    MaisVariaveis();
                }
            }
        } else if ((atual() != null) && (atual().getLexema().equals(";"))) {
            posicaoAtual = posicaoAtual + 1;
            OutrasVariaveis();
        } else {
            addErro(atual(), "',' ou ';'");
            while (!(atual().getLexema().equals("}")
                    || atual().getLexema().equals("$"))) {
                posicaoAtual = posicaoAtual + 1;
            }
            if (atual().getLexema().equals("$")) {
                addErro(atual(), "fim de programa");
            }
        }

    }
    // <OutrasVariaveis> ::= Tipo Identificador <Matriz> <MaisVariaveis> | <>

    public void OutrasVariaveis() {

        tabela.addvar();

        if ((atual() != null) && (tipos.contains(atual().getLexema()))) {

            tabela.setTipovar(atual());

            posicaoAtual = posicaoAtual + 1;
            if ((atual() != null) && (atual().getTipo().equals("IDE"))) {

                tabela.setIdevar(atual());
                tabela.addListavar();

                posicaoAtual = posicaoAtual + 1;
                Matriz();
                MaisVariaveis();
            } else {
                addErro(atual(), "IDE");
                while (!(atual().getLexema().equals(",")
                        || atual().getLexema().equals(";")
                        || atual().getLexema().equals("$"))) {
                    posicaoAtual = posicaoAtual + 1;
                }
                if (atual().getLexema().equals("$")) {
                    addErro(atual(), "fim de programa");
                } else {
                    MaisVariaveis();
                }
            }
        }
    }

    // <Funcoes> ::= 'function' Tipo Identificador '(' <Parametros> ')'
    // '{' <Variaveis> <Comandos> '.' Identificador '=' <Valores> ';' '}' <Funcoes>
    public void Funcoes() {
        if ((atual() != null) && (atual().getLexema().equals("function"))) {
            posicaoAtual = posicaoAtual + 1;

            tabela.addFun();
            tabela.setTipoFun(atual());

            if ((atual() != null) && (tipos.contains(atual().getLexema()))) {
                posicaoAtual = posicaoAtual + 1;

                tabela.setIdeFun(atual());

                if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                    posicaoAtual = posicaoAtual + 1;
                    if ((atual() != null) && (atual().getLexema().equals("("))) {
                        posicaoAtual = posicaoAtual + 1;
                        Parametros();
                        if ((atual() != null) && (atual().getLexema().equals(")"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Complemento();
                        } else {
                            addErro(atual(), "')'");
                            if (atual().getLexema().equals("{") || seguinte().getLexema().equals(")")) {
                                if (seguinte().getLexema().equals(")")) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Complemento();
                                } else {
                                    Complemento();
                                }
                            } else {
                                while (!(atual().getLexema().equals("{")
                                        || atual().getLexema().equals("}")
                                        || atual().getLexema().equals(".")
                                        || atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("var")
                                        || atual().getLexema().equals("for")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                }
                            }
                            Complemento();
                        }

                    } else {
                        addErro(atual(), "'('");
                        if (seguinte().getLexema().equals("(") || tipos.contains(atual().getLexema())) {
                            if (seguinte().getLexema().equals("(")) {
                                posicaoAtual = posicaoAtual + 1;
                                Parametros();
                            } else {
                                Parametros();
                            }

                        } else {
                            while (!(tipos.contains(atual().getLexema())
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals(",")
                                    || atual().getLexema().equals(";")
                                    || atual().getLexema().equals(")")
                                    || atual().getLexema().equals(".")
                                    || atual().getLexema().equals("{")
                                    || atual().getLexema().equals("}")
                                    || atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("var")
                                    || atual().getLexema().equals("for")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            }
                            Parametros();
                        }
                        if ((atual() != null) && (atual().getLexema().equals(")"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Complemento();
                        } else {
                            addErro(atual(), "')'");
                            if (atual().getLexema().equals("{") || seguinte().getLexema().equals(")")) {
                                if (seguinte().getLexema().equals(")")) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Complemento();
                                } else {
                                    Complemento();
                                }
                            } else {
                                while (!(atual().getLexema().equals("{")
                                        || atual().getLexema().equals("}")
                                        || atual().getLexema().equals(".")
                                        || atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("var")
                                        || atual().getLexema().equals("for")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                }
                            }
                            Complemento();
                        }
                    }

                } else {
                    addErro(atual(), "IDE");
                    if (seguinte().getTipo().equals("IDE")) {
                        posicaoAtual = posicaoAtual + 1;
                    } else {
                        while (!(atual().getLexema().equals("(")
                                || tipos.contains(atual().getLexema())
                                || atual().getLexema().equals("{")
                                || atual().getLexema().equals(")")
                                || atual().getLexema().equals("}")
                                || atual().getLexema().equals(".")
                                || atual().getLexema().equals("write")
                                || atual().getLexema().equals("read")
                                || atual().getLexema().equals("var")
                                || atual().getLexema().equals("for")
                                || atual().getTipo().equals("IDE")
                                || atual().getLexema().equals("if")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if (atual().getLexema().equals("$")) {
                            addErro(atual(), "fim de programa");
                        }
                    }

                    if ((atual() != null) && (atual().getLexema().equals("("))) {
                        posicaoAtual = posicaoAtual + 1;
                        Parametros();
                        if ((atual() != null) && (atual().getLexema().equals(")"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Complemento();
                        } else {
                            addErro(atual(), "')'");
                            if (atual().getLexema().equals("{") || seguinte().getLexema().equals(")")) {
                                if (seguinte().getLexema().equals(")")) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Complemento();
                                } else {
                                    Complemento();
                                }
                            } else {
                                while (!(atual().getLexema().equals("{")
                                        || atual().getLexema().equals("}")
                                        || atual().getLexema().equals(".")
                                        || atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("var")
                                        || atual().getLexema().equals("for")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                }
                            }
                            Complemento();
                        }

                    } else {
                        addErro(atual(), "'('");
                        if (seguinte().getLexema().equals("(") || tipos.contains(atual().getLexema())) {
                            if (seguinte().getLexema().equals("(")) {
                                posicaoAtual = posicaoAtual + 1;
                                Parametros();
                            } else {
                                Parametros();
                            }

                        } else {
                            while (!(tipos.contains(atual().getLexema())
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals(",")
                                    || atual().getLexema().equals(";")
                                    || atual().getLexema().equals(")")
                                    || atual().getLexema().equals(".")
                                    || atual().getLexema().equals("{")
                                    || atual().getLexema().equals("}")
                                    || atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("var")
                                    || atual().getLexema().equals("for")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            }
                            Parametros();
                        }
                        if ((atual() != null) && (atual().getLexema().equals(")"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Complemento();
                        } else {
                            addErro(atual(), "')'");
                            if (atual().getLexema().equals("{") || seguinte().getLexema().equals(")")) {
                                if (seguinte().getLexema().equals(")")) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Complemento();
                                } else {
                                    Complemento();
                                }
                            } else {
                                while (!(atual().getLexema().equals("{")
                                        || atual().getLexema().equals("}")
                                        || atual().getLexema().equals(".")
                                        || atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("var")
                                        || atual().getLexema().equals("for")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                }
                            }
                            Complemento();
                        }
                    }

                }
            } else {
                addErro(atual(), "tipo");
                if (tipos.contains(seguinte().getLexema())) {
                    posicaoAtual = posicaoAtual + 1;
                } else {
                    while (!(atual().getLexema().equals("{")
                            || atual().getLexema().equals("}")
                            || atual().getLexema().equals(")")
                            || atual().getLexema().equals("(")
                            || atual().getLexema().equals(".")
                            || atual().getLexema().equals("write")
                            || atual().getLexema().equals("read")
                            || atual().getLexema().equals("var")
                            || atual().getLexema().equals("for")
                            || atual().getTipo().equals("IDE")
                            || atual().getLexema().equals("if")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "fim de programa");
                    }
                }
                if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                    posicaoAtual = posicaoAtual + 1;
                    if ((atual() != null) && (atual().getLexema().equals("("))) {
                        posicaoAtual = posicaoAtual + 1;
                        Parametros();
                        if ((atual() != null) && (atual().getLexema().equals(")"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Complemento();
                        } else {
                            addErro(atual(), "')'");
                            if (atual().getLexema().equals("{") || seguinte().getLexema().equals(")")) {
                                if (seguinte().getLexema().equals(")")) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Complemento();
                                } else {
                                    Complemento();
                                }
                            } else {
                                while (!(atual().getLexema().equals("{")
                                        || atual().getLexema().equals("}")
                                        || atual().getLexema().equals(".")
                                        || atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("var")
                                        || atual().getLexema().equals("for")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                }
                            }
                            Complemento();
                        }

                    } else {
                        addErro(atual(), "'('");
                        if (seguinte().getLexema().equals("(") || tipos.contains(atual().getLexema())) {
                            if (seguinte().getLexema().equals("(")) {
                                posicaoAtual = posicaoAtual + 1;
                                Parametros();
                            } else {
                                Parametros();
                            }

                        } else {
                            while (!(tipos.contains(atual().getLexema())
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals(",")
                                    || atual().getLexema().equals(";")
                                    || atual().getLexema().equals(")")
                                    || atual().getLexema().equals(".")
                                    || atual().getLexema().equals("{")
                                    || atual().getLexema().equals("}")
                                    || atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("var")
                                    || atual().getLexema().equals("for")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            }
                            Parametros();
                        }
                        if ((atual() != null) && (atual().getLexema().equals(")"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Complemento();
                        } else {
                            addErro(atual(), "')'");
                            if (atual().getLexema().equals("{") || seguinte().getLexema().equals(")")) {
                                if (seguinte().getLexema().equals(")")) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Complemento();
                                } else {
                                    Complemento();
                                }
                            } else {
                                while (!(atual().getLexema().equals("{")
                                        || atual().getLexema().equals("}")
                                        || atual().getLexema().equals(".")
                                        || atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("var")
                                        || atual().getLexema().equals("for")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                }
                            }
                            Complemento();
                        }
                    }

                } else {
                    addErro(atual(), "IDE");
                    if (seguinte().getTipo().equals("IDE")) {
                        posicaoAtual = posicaoAtual + 1;
                    } else {
                        while (!(atual().getLexema().equals("(")
                                || tipos.contains(atual().getLexema())
                                || atual().getLexema().equals("{")
                                || atual().getLexema().equals(")")
                                || atual().getLexema().equals("}")
                                || atual().getLexema().equals(".")
                                || atual().getLexema().equals("write")
                                || atual().getLexema().equals("read")
                                || atual().getLexema().equals("var")
                                || atual().getLexema().equals("for")
                                || atual().getTipo().equals("IDE")
                                || atual().getLexema().equals("if")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if (atual().getLexema().equals("$")) {
                            addErro(atual(), "fim de programa");
                        }
                    }

                    /**/ if ((atual() != null) && (atual().getLexema().equals("("))) {
                        posicaoAtual = posicaoAtual + 1;
                        Parametros();
                        if ((atual() != null) && (atual().getLexema().equals(")"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Complemento();
                        } else {
                            addErro(atual(), "')'");
                            if (atual().getLexema().equals("{") || seguinte().getLexema().equals(")")) {
                                if (seguinte().getLexema().equals(")")) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Complemento();
                                } else {
                                    Complemento();
                                }
                            } else {
                                while (!(atual().getLexema().equals("{")
                                        || atual().getLexema().equals("}")
                                        || atual().getLexema().equals(".")
                                        || atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("var")
                                        || atual().getLexema().equals("for")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                }
                            }
                            Complemento();
                        }

                    } else {
                        addErro(atual(), "'('");
                        if (seguinte().getLexema().equals("(") || tipos.contains(atual().getLexema())) {
                            if (seguinte().getLexema().equals("(")) {
                                posicaoAtual = posicaoAtual + 1;
                                Parametros();
                            } else {
                                Parametros();
                            }

                        } else {
                            while (!(tipos.contains(atual().getLexema())
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals(",")
                                    || atual().getLexema().equals(";")
                                    || atual().getLexema().equals(")")
                                    || atual().getLexema().equals(".")
                                    || atual().getLexema().equals("{")
                                    || atual().getLexema().equals("}")
                                    || atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("var")
                                    || atual().getLexema().equals("for")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            }
                            Parametros();
                        }
                        if ((atual() != null) && (atual().getLexema().equals(")"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Complemento();
                        } else {
                            addErro(atual(), "')'");
                            if (atual().getLexema().equals("{") || seguinte().getLexema().equals(")")) {
                                if (seguinte().getLexema().equals(")")) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Complemento();
                                } else {
                                    Complemento();
                                }
                            } else {
                                while (!(atual().getLexema().equals("{")
                                        || atual().getLexema().equals("}")
                                        || atual().getLexema().equals(".")
                                        || atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("var")
                                        || atual().getLexema().equals("for")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                }
                            }
                            Complemento();
                        }
                    }

                }
            }

        } else if ((atual() != null) && (atual().getLexema().equals("procedure"))) {
            Procedure();
        }
    }
    // | 'procedure' Identificador '(' <Parametros> ')' '{' <Variaveis> <Comandos>
    // '}' <Funcoes> | <>

    public void Procedure() {
        if ((atual() != null) && (atual().getLexema().equals("procedure"))) {

            Token proc = new Token(0, 0, "proc", "proc");
            tabela.addFun();
            tabela.setTipoFun(proc);

            posicaoAtual = posicaoAtual + 1;
            if ((atual() != null) && (atual().getTipo().equals("IDE"))) {

                tabela.setIdeFun(atual());
                tabela.setRetornoFun(proc);
                tabela.setRetornoFunAtr(proc);

                posicaoAtual = posicaoAtual + 1;
                if ((atual() != null) && (atual().getLexema().equals("("))) {
                    posicaoAtual = posicaoAtual + 1;
                    Parametros();
                    if ((atual() != null) && (atual().getLexema().equals(")"))) {
                        posicaoAtual = posicaoAtual + 1;
                        if ((atual() != null) && (atual().getLexema().equals("{"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Variaveis();

                            tabela.addListaFun();

                            Comandos();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Funcoes();
                            } else {

                                addErro(atual(), "'}'");
                                Funcoes();
                            }
                        } else {
                            addErro(atual(), "'{'");
                            if (atual().getLexema().equals("var")) {
                                Variaveis();
                            } else if (atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("if")
                                    || atual().getTipo().equals("IDE")) {
                                Comandos();
                            } else {
                                while (!(atual().getLexema().equals("var")
                                        || atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("for")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("}")
                                        || atual().getLexema().equals("$")
                                        || atual().getTipo().equals("IDE"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                Variaveis();
                                Comandos();
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }
                            }
                        }
                    } else {
                        addErro(atual(), "')'");
                        if (seguinte().getLexema().equals(")")) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            while (!(atual().getLexema().equals("{")
                                    || atual().getLexema().equals("}")
                                    || atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("var")
                                    || atual().getLexema().equals("for")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            }
                        }
                        if ((atual() != null) && (atual().getLexema().equals("{"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Variaveis();
                            Comandos();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Funcoes();
                            } else {
                                addErro(atual(), "'}'");
                                Funcoes();
                            }
                        } else {
                            addErro(atual(), "'{'");
                            if (atual().getLexema().equals("var")) {
                                Variaveis();
                            } else if (atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("if")
                                    || atual().getTipo().equals("IDE")) {
                                Comandos();
                            } else {
                                while (!(atual().getLexema().equals("var")
                                        || atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("for")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("}")
                                        || atual().getLexema().equals("$")
                                        || atual().getTipo().equals("IDE"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                Variaveis();
                                Comandos();
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }
                            }
                        }
                    }

                } else {
                    addErro(atual(), "'('");
                    if (seguinte().getLexema().equals("(") || tipos.contains(atual().getLexema())) {
                        if (seguinte().getLexema().equals("(")) {
                            posicaoAtual = posicaoAtual + 1;
                            Parametros();
                        } else {
                            Parametros();
                        }

                    } else {
                        while (!(tipos.contains(atual().getLexema())
                                || atual().getTipo().equals("IDE")
                                || atual().getLexema().equals(",")
                                || atual().getLexema().equals(";")
                                || atual().getLexema().equals(")")
                                || atual().getLexema().equals("{")
                                || atual().getLexema().equals("}")
                                || atual().getLexema().equals("write")
                                || atual().getLexema().equals("read")
                                || atual().getLexema().equals("var")
                                || atual().getLexema().equals("for")
                                || atual().getTipo().equals("IDE")
                                || atual().getLexema().equals("if")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if (atual().getLexema().equals("$")) {
                            addErro(atual(), "fim de programa");
                        }
                        Parametros();
                    }
                    if ((atual() != null) && (atual().getLexema().equals(")"))) {
                        posicaoAtual = posicaoAtual + 1;
                        if ((atual() != null) && (atual().getLexema().equals("{"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Variaveis();
                            Comandos();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Funcoes();
                            } else {
                                addErro(atual(), "'}'");
                                Funcoes();
                            }
                        } else {
                            addErro(atual(), "'{'");
                            if (atual().getLexema().equals("var")) {
                                Variaveis();
                            } else if (atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("if")
                                    || atual().getTipo().equals("IDE")) {
                                Comandos();
                            } else {
                                while (!(atual().getLexema().equals("var")
                                        || atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("for")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("}")
                                        || atual().getLexema().equals("$")
                                        || atual().getTipo().equals("IDE"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                Variaveis();
                                Comandos();
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }
                            }
                        }
                    } else {
                        addErro(atual(), "')'");
                        if (seguinte().getLexema().equals(")")) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            while (!(atual().getLexema().equals("{")
                                    || atual().getLexema().equals("}")
                                    || atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("var")
                                    || atual().getLexema().equals("for")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            }
                        }
                        if ((atual() != null) && (atual().getLexema().equals("{"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Variaveis();
                            Comandos();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Funcoes();
                            } else {
                                addErro(atual(), "'}'");
                                Funcoes();
                            }
                        } else {
                            addErro(atual(), "'{'");
                            if (atual().getLexema().equals("var")) {
                                Variaveis();
                            } else if (atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("if")
                                    || atual().getTipo().equals("IDE")) {
                                Comandos();
                            } else {
                                while (!(atual().getLexema().equals("var")
                                        || atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("for")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("}")
                                        || atual().getLexema().equals("$")
                                        || atual().getTipo().equals("IDE"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                Variaveis();
                                Comandos();
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }
                            }
                        }
                    }
                }
            } else {
                addErro(atual(), "IDE");
                if (seguinte().getTipo().equals("IDE")) {
                    posicaoAtual = posicaoAtual + 1;
                } else {
                    while (!(atual().getLexema().equals("(")
                            || tipos.contains(atual().getLexema())
                            || atual().getLexema().equals("{")
                            || atual().getLexema().equals(")")
                            || atual().getLexema().equals("}")
                            || atual().getLexema().equals("write")
                            || atual().getLexema().equals("read")
                            || atual().getLexema().equals("var")
                            || atual().getLexema().equals("for")
                            || atual().getTipo().equals("IDE")
                            || atual().getLexema().equals("if")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "fim de programa");
                    }
                }
                if ((atual() != null) && (atual().getLexema().equals("("))) {
                    posicaoAtual = posicaoAtual + 1;
                    Parametros();
                    if ((atual() != null) && (atual().getLexema().equals(")"))) {
                        posicaoAtual = posicaoAtual + 1;
                        if ((atual() != null) && (atual().getLexema().equals("{"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Variaveis();
                            Comandos();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Funcoes();
                            } else {
                                addErro(atual(), "'}'");
                                Funcoes();
                            }
                        } else {
                            addErro(atual(), "'{'");
                            if (atual().getLexema().equals("var")) {
                                Variaveis();
                            } else if (atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("if")
                                    || atual().getTipo().equals("IDE")) {
                                Comandos();
                            } else {
                                while (!(atual().getLexema().equals("var")
                                        || atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("for")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("}")
                                        || atual().getLexema().equals("$")
                                        || atual().getTipo().equals("IDE"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                Variaveis();
                                Comandos();
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }
                            }
                        }
                    } else {
                        addErro(atual(), "')'");
                        if (seguinte().getLexema().equals(")")) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            while (!(atual().getLexema().equals("{")
                                    || atual().getLexema().equals("}")
                                    || atual().getLexema().equals(".")
                                    || atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("var")
                                    || atual().getLexema().equals("for")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            }
                        }
                        if ((atual() != null) && (atual().getLexema().equals("{"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Variaveis();
                            Comandos();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Funcoes();
                            } else {
                                addErro(atual(), "'}'");
                                Funcoes();
                            }
                        } else {
                            addErro(atual(), "'{'");
                            if (atual().getLexema().equals("var")) {
                                Variaveis();
                            } else if (atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("if")
                                    || atual().getTipo().equals("IDE")) {
                                Comandos();
                            } else {
                                while (!(atual().getLexema().equals("var")
                                        || atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("for")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("}")
                                        || atual().getLexema().equals("$")
                                        || atual().getTipo().equals("IDE"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                Variaveis();
                                Comandos();
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }
                            }
                        }
                    }

                } else {

                    addErro(atual(), "'('");
                    if (seguinte().getLexema().equals("(") || tipos.contains(atual().getLexema())) {
                        if (seguinte().getLexema().equals("(")) {
                            posicaoAtual = posicaoAtual + 1;
                            Parametros();
                        } else {
                            Parametros();
                        }

                    } else {
                        while (!(tipos.contains(atual().getLexema())
                                || atual().getTipo().equals("IDE")
                                || atual().getLexema().equals(",")
                                || atual().getLexema().equals(";")
                                || atual().getLexema().equals(")")
                                || atual().getLexema().equals("{")
                                || atual().getLexema().equals("}")
                                || atual().getLexema().equals("write")
                                || atual().getLexema().equals("read")
                                || atual().getLexema().equals("var")
                                || atual().getLexema().equals("for")
                                || atual().getTipo().equals("IDE")
                                || atual().getLexema().equals("if")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if (atual().getLexema().equals("$")) {
                            addErro(atual(), "fim de programa");
                        }
                        Parametros();
                    }
                    if ((atual() != null) && (atual().getLexema().equals(")"))) {
                        posicaoAtual = posicaoAtual + 1;
                        if ((atual() != null) && (atual().getLexema().equals("{"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Variaveis();
                            Comandos();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Funcoes();
                            } else {
                                addErro(atual(), "'}'");
                                Funcoes();
                            }
                        } else {
                            addErro(atual(), "'{'");
                            if (atual().getLexema().equals("var")) {
                                Variaveis();
                            } else if (atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("if")
                                    || atual().getTipo().equals("IDE")) {
                                Comandos();
                            } else {
                                while (!(atual().getLexema().equals("var")
                                        || atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("for")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("}")
                                        || atual().getLexema().equals("$")
                                        || atual().getTipo().equals("IDE"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                Variaveis();
                                Comandos();
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }
                            }
                        }
                    } else {
                        addErro(atual(), "')'");
                        if (seguinte().getLexema().equals(")")) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            while (!(atual().getLexema().equals("{")
                                    || atual().getLexema().equals("}")
                                    || atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("var")
                                    || atual().getLexema().equals("for")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            }
                        }
                        if ((atual() != null) && (atual().getLexema().equals("{"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Variaveis();
                            Comandos();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Funcoes();
                            } else {
                                addErro(atual(), "'}'");
                                Funcoes();
                            }
                        } else {
                            addErro(atual(), "'{'");
                            if (atual().getLexema().equals("var")) {
                                Variaveis();
                            } else if (atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("if")
                                    || atual().getTipo().equals("IDE")) {
                                Comandos();
                            } else {
                                while (!(atual().getLexema().equals("var")
                                        || atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("for")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("}")
                                        || atual().getLexema().equals("$")
                                        || atual().getTipo().equals("IDE"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                Variaveis();
                                Comandos();
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // '{' <Variaveis> <Comandos> '.' Identificador '=' <Valores> ';' '}' <Funcoes>
    public void Complemento() {
        if ((atual() != null) && (atual().getLexema().equals("{"))) {
            posicaoAtual = posicaoAtual + 1;
            Variaveis();
            Comandos();
            if ((atual() != null) && (atual().getLexema().equals("."))) {
                posicaoAtual = posicaoAtual + 1;
                if ((atual() != null) && (atual().getTipo().equals("IDE"))) {

                    tabela.setRetornoFun(atual());
                    tabela.verificaRetornoDeclarado(atual());

                    posicaoAtual = posicaoAtual + 1;
                    if ((atual() != null) && (atual().getLexema().equals("="))) {
                        posicaoAtual = posicaoAtual + 1;

                        tabela.setRetornoFunAtr(atual());
                        tabela.addListaFun();

                        valores();
                        if ((atual() != null) && (atual().getLexema().equals(";"))) {
                            posicaoAtual = posicaoAtual + 1;
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Funcoes();
                            } else {
                                addErro(atual(), "'}'");
                                Funcoes();
                            }

                        } else {
                            addErro(atual(), "';'");
                            while (!(atual().getLexema().equals("}")
                                    || atual().getLexema().equals("function")
                                    || atual().getLexema().equals("procedure")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Funcoes();
                            } else {
                                addErro(atual(), "'}'");
                                Funcoes();
                            }
                        }
                    } else {
                        addErro(atual(), "'='");
                        while (!(atual().getLexema().equals("}")
                                || atual().getTipo().equals("IDE")
                                || atual().getTipo().equals("string")
                                || atual().getTipo().equals("float")
                                || atual().getTipo().equals("int")
                                || atual().getLexema().equals("true")
                                || atual().getLexema().equals("false")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if ((atual().getLexema().equals("$"))) {
                            addErro(atual(), "fim de problema");
                        } else {
                            valores();
                            if ((atual() != null) && (atual().getLexema().equals(";"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }

                            } else {
                                addErro(atual(), "';'");
                                while (!(atual().getLexema().equals("}")
                                        || atual().getLexema().equals("function")
                                        || atual().getLexema().equals("procedure")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if ((atual().getLexema().equals("$"))) {
                                    addErro(atual(), "fim de problema");
                                } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }
                            }
                        }
                    }
                } else {
                    addErro(atual(), "IDE");
                    while (!(atual().getLexema().equals("=")
                            || atual().getTipo().equals("IDE")
                            || atual().getTipo().equals("string")
                            || atual().getTipo().equals("float")
                            || atual().getTipo().equals("int")
                            || atual().getLexema().equals("true")
                            || atual().getLexema().equals("false")
                            || atual().getLexema().equals("}")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if ((atual().getLexema().equals("$"))) {
                        addErro(atual(), "fim de problema");
                    } else if ((atual() != null) && (atual().getLexema().equals("="))) {
                        posicaoAtual = posicaoAtual + 1;
                        valores();
                        if ((atual() != null) && (atual().getLexema().equals(";"))) {
                            posicaoAtual = posicaoAtual + 1;
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Funcoes();
                            } else {
                                addErro(atual(), "'}'");
                                Funcoes();
                            }

                        } else {
                            addErro(atual(), "';'");
                            while (!(atual().getLexema().equals("}")
                                    || atual().getLexema().equals("function")
                                    || atual().getLexema().equals("procedure")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Funcoes();
                            } else {
                                addErro(atual(), "'}'");
                                Funcoes();
                            }
                        }
                    } else {
                        addErro(atual(), "'='");
                        while (!(atual().getLexema().equals("}")
                                || atual().getTipo().equals("IDE")
                                || atual().getTipo().equals("string")
                                || atual().getTipo().equals("float")
                                || atual().getTipo().equals("int")
                                || atual().getLexema().equals("true")
                                || atual().getLexema().equals("false")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if ((atual().getLexema().equals("$"))) {
                            addErro(atual(), "fim de problema");
                        } else {
                            valores();
                            if ((atual() != null) && (atual().getLexema().equals(";"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }

                            } else {
                                addErro(atual(), "';'");
                                while (!(atual().getLexema().equals("}")
                                        || atual().getLexema().equals("function")
                                        || atual().getLexema().equals("procedure")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if ((atual().getLexema().equals("$"))) {
                                    addErro(atual(), "fim de problema");
                                } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }
                            }
                        }
                    }
                }
            } else {
                addErro(atual(), "'.'");
                while (!(atual().getLexema().equals("}")
                        || atual().getLexema().equals(".")
                        || atual().getTipo().equals("IDE")
                        || atual().getTipo().equals("string")
                        || atual().getTipo().equals("float")
                        || atual().getTipo().equals("int")
                        || atual().getLexema().equals("true")
                        || atual().getLexema().equals("false")
                        || atual().getLexema().equals("$"))) {
                    posicaoAtual = posicaoAtual + 1;
                }
                if ((atual().getLexema().equals("$"))) {
                    addErro(atual(), "fim de problema");
                } else if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                    posicaoAtual = posicaoAtual + 1;
                    if ((atual() != null) && (atual().getLexema().equals("="))) {
                        posicaoAtual = posicaoAtual + 1;
                        valores();
                        if ((atual() != null) && (atual().getLexema().equals(";"))) {
                            posicaoAtual = posicaoAtual + 1;
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Funcoes();
                            } else {
                                addErro(atual(), "'}'");
                                Funcoes();
                            }

                        } else {
                            addErro(atual(), "';'");
                            while (!(atual().getLexema().equals("}")
                                    || atual().getLexema().equals("function")
                                    || atual().getLexema().equals("procedure")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Funcoes();
                            } else {
                                addErro(atual(), "'}'");
                                Funcoes();
                            }
                        }
                    } else {
                        addErro(atual(), "'='");
                        while (!(atual().getLexema().equals("}")
                                || atual().getTipo().equals("IDE")
                                || atual().getLexema().equals("=")
                                || atual().getTipo().equals("string")
                                || atual().getTipo().equals("float")
                                || atual().getTipo().equals("int")
                                || atual().getLexema().equals("true")
                                || atual().getLexema().equals("false")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if ((atual().getLexema().equals("$"))) {
                            addErro(atual(), "fim de problema");
                        } else {
                            valores();
                            if ((atual() != null) && (atual().getLexema().equals(";"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }

                            } else {
                                addErro(atual(), "';'");
                                while (!(atual().getLexema().equals("}")
                                        || atual().getLexema().equals(";")
                                        || atual().getLexema().equals("function")
                                        || atual().getLexema().equals("procedure")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if ((atual().getLexema().equals("$"))) {
                                    addErro(atual(), "fim de problema");
                                } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }
                            }
                        }
                    }
                } else {
                    addErro(atual(), "IDE");
                    while (!(atual().getLexema().equals("=")
                            || atual().getTipo().equals("IDE")
                            || atual().getTipo().equals("string")
                            || atual().getTipo().equals("float")
                            || atual().getTipo().equals("int")
                            || atual().getLexema().equals("true")
                            || atual().getLexema().equals("false")
                            || atual().getLexema().equals("}")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if ((atual().getLexema().equals("$"))) {
                        addErro(atual(), "fim de problema");
                    } else if ((atual() != null) && (atual().getLexema().equals("="))) {
                        posicaoAtual = posicaoAtual + 1;
                        valores();
                        if ((atual() != null) && (atual().getLexema().equals(";"))) {
                            posicaoAtual = posicaoAtual + 1;
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Funcoes();
                            } else {
                                addErro(atual(), "'}'");
                                Funcoes();
                            }

                        } else {
                            addErro(atual(), "';'");
                            while (!(atual().getLexema().equals("}")
                                    || atual().getLexema().equals(";")
                                    || atual().getLexema().equals("function")
                                    || atual().getLexema().equals("procedure")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Funcoes();
                            } else {
                                addErro(atual(), "'}'");
                                Funcoes();
                            }
                        }
                    } else {
                        addErro(atual(), "'='");
                        while (!(atual().getLexema().equals("}")
                                || atual().getTipo().equals("IDE")
                                || atual().getTipo().equals("string")
                                || atual().getLexema().equals("=")
                                || atual().getTipo().equals("float")
                                || atual().getTipo().equals("int")
                                || atual().getLexema().equals("true")
                                || atual().getLexema().equals("false")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if ((atual().getLexema().equals("$"))) {
                            addErro(atual(), "fim de problema");
                        } else {
                            valores();
                            if ((atual() != null) && (atual().getLexema().equals(";"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }

                            } else {
                                addErro(atual(), "';'");
                                while (!(atual().getLexema().equals("}")
                                        || atual().getLexema().equals(";")
                                        || atual().getLexema().equals("function")
                                        || atual().getLexema().equals("procedure")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if ((atual().getLexema().equals("$"))) {
                                    addErro(atual(), "fim de problema");
                                } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }
                            }
                        }
                    }
                }
            }
        } else {
            addErro(atual(), "'{'");
            if (atual().getLexema().equals("var")) {
                Variaveis();
            } else if (atual().getLexema().equals("write")
                    || atual().getLexema().equals("read")
                    || atual().getLexema().equals("for")
                    || atual().getLexema().equals("if")
                    || atual().getTipo().equals("IDE")) {
                Comandos();
            } else {
                while (!(atual().getLexema().equals("var")
                        || atual().getLexema().equals(".")
                        || atual().getLexema().equals("write")
                        || atual().getLexema().equals("read")
                        || atual().getLexema().equals("for")
                        || atual().getLexema().equals("if")
                        || atual().getLexema().equals("}")
                        || atual().getLexema().equals("$")
                        || atual().getTipo().equals("IDE"))) {
                    posicaoAtual = posicaoAtual + 1;
                }
                Variaveis();
                Comandos();

                if ((atual() != null) && (atual().getLexema().equals("."))) {
                    posicaoAtual = posicaoAtual + 1;
                    if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                        posicaoAtual = posicaoAtual + 1;
                        if ((atual() != null) && (atual().getLexema().equals("="))) {
                            posicaoAtual = posicaoAtual + 1;
                            valores();
                            if ((atual() != null) && (atual().getLexema().equals(";"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }

                            } else {
                                addErro(atual(), "';'");
                                while (!(atual().getLexema().equals("}")
                                        || atual().getLexema().equals("function")
                                        || atual().getLexema().equals("procedure")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if ((atual().getLexema().equals("$"))) {
                                    addErro(atual(), "fim de problema");
                                } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }
                            }
                        } else {
                            addErro(atual(), "'='");
                            while (!(atual().getLexema().equals("}")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getTipo().equals("string")
                                    || atual().getTipo().equals("float")
                                    || atual().getTipo().equals("int")
                                    || atual().getLexema().equals("true")
                                    || atual().getLexema().equals("false")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else {
                                valores();
                                if ((atual() != null) && (atual().getLexema().equals(";"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                        Funcoes();
                                    } else {
                                        addErro(atual(), "'}'");
                                        Funcoes();
                                    }

                                } else {
                                    addErro(atual(), "';'");
                                    while (!(atual().getLexema().equals("}")
                                            || atual().getLexema().equals("$"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    }
                                    if ((atual().getLexema().equals("$"))) {
                                        addErro(atual(), "fim de problema");
                                    } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                        Funcoes();
                                    } else {
                                        addErro(atual(), "'}'");
                                        Funcoes();
                                    }
                                }
                            }
                        }
                    } else {
                        addErro(atual(), "IDE");
                        while (!(atual().getLexema().equals("=")
                                || atual().getTipo().equals("IDE")
                                || atual().getTipo().equals("string")
                                || atual().getTipo().equals("float")
                                || atual().getTipo().equals("int")
                                || atual().getLexema().equals("true")
                                || atual().getLexema().equals("false")
                                || atual().getLexema().equals("}")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if ((atual().getLexema().equals("$"))) {
                            addErro(atual(), "fim de problema");
                        } else if ((atual() != null) && (atual().getLexema().equals("="))) {
                            posicaoAtual = posicaoAtual + 1;
                            valores();
                            if ((atual() != null) && (atual().getLexema().equals(";"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }

                            } else {
                                addErro(atual(), "';'");
                                while (!(atual().getLexema().equals("}")
                                        || atual().getLexema().equals("function")
                                        || atual().getLexema().equals("procedure")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if ((atual().getLexema().equals("$"))) {
                                    addErro(atual(), "fim de problema");
                                } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }
                            }
                        } else {
                            addErro(atual(), "'='");
                            while (!(atual().getLexema().equals("}")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getTipo().equals("string")
                                    || atual().getTipo().equals("float")
                                    || atual().getTipo().equals("int")
                                    || atual().getLexema().equals("true")
                                    || atual().getLexema().equals("false")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else {
                                valores();
                                if ((atual() != null) && (atual().getLexema().equals(";"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                        Funcoes();
                                    } else {
                                        addErro(atual(), "'}'");
                                        Funcoes();
                                    }

                                } else {
                                    addErro(atual(), "';'");
                                    while (!(atual().getLexema().equals("}")
                                            || atual().getLexema().equals("function")
                                            || atual().getLexema().equals("procedure")
                                            || atual().getLexema().equals("$"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    }
                                    if ((atual().getLexema().equals("$"))) {
                                        addErro(atual(), "fim de problema");
                                    } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                        Funcoes();
                                    } else {
                                        addErro(atual(), "'}'");
                                        Funcoes();
                                    }
                                }
                            }
                        }
                    }
                } else {
                    addErro(atual(), "'.'");
                    while (!(atual().getLexema().equals("}")
                            || atual().getTipo().equals("IDE")
                            || atual().getTipo().equals("string")
                            || atual().getTipo().equals("float")
                            || atual().getTipo().equals("int")
                            || atual().getLexema().equals("true")
                            || atual().getLexema().equals("false")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if ((atual().getLexema().equals("$"))) {
                        addErro(atual(), "fim de problema");
                    } else if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                        posicaoAtual = posicaoAtual + 1;
                        if ((atual() != null) && (atual().getLexema().equals("="))) {
                            posicaoAtual = posicaoAtual + 1;
                            valores();
                            if ((atual() != null) && (atual().getLexema().equals(";"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }

                            } else {
                                addErro(atual(), "';'");
                                while (!(atual().getLexema().equals("}")
                                        || atual().getLexema().equals("function")
                                        || atual().getLexema().equals("procedure")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if ((atual().getLexema().equals("$"))) {
                                    addErro(atual(), "fim de problema");
                                } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }
                            }
                        } else {
                            addErro(atual(), "'='");
                            while (!(atual().getLexema().equals("}")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getTipo().equals("string")
                                    || atual().getTipo().equals("float")
                                    || atual().getTipo().equals("int")
                                    || atual().getLexema().equals("true")
                                    || atual().getLexema().equals("false")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else {
                                valores();
                                if ((atual() != null) && (atual().getLexema().equals(";"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                        Funcoes();
                                    } else {
                                        addErro(atual(), "'}'");
                                        Funcoes();
                                    }

                                } else {
                                    addErro(atual(), "';'");
                                    while (!(atual().getLexema().equals("}")
                                            || atual().getLexema().equals("function")
                                            || atual().getLexema().equals("procedure")
                                            || atual().getLexema().equals("$"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    }
                                    if ((atual().getLexema().equals("$"))) {
                                        addErro(atual(), "fim de problema");
                                    } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                        Funcoes();
                                    } else {
                                        addErro(atual(), "'}'");
                                        Funcoes();
                                    }
                                }
                            }
                        }
                    } else {
                        addErro(atual(), "IDE");
                        while (!(atual().getLexema().equals("=")
                                || atual().getTipo().equals("IDE")
                                || atual().getTipo().equals("string")
                                || atual().getTipo().equals("float")
                                || atual().getTipo().equals("int")
                                || atual().getLexema().equals("true")
                                || atual().getLexema().equals("false")
                                || atual().getLexema().equals("}")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if ((atual().getLexema().equals("$"))) {
                            addErro(atual(), "fim de problema");
                        } else if ((atual() != null) && (atual().getLexema().equals("="))) {
                            posicaoAtual = posicaoAtual + 1;
                            valores();
                            if ((atual() != null) && (atual().getLexema().equals(";"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }

                            } else {
                                addErro(atual(), "';'");
                                while (!(atual().getLexema().equals("}")
                                        || atual().getLexema().equals("function")
                                        || atual().getLexema().equals("procedure")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if ((atual().getLexema().equals("$"))) {
                                    addErro(atual(), "fim de problema");
                                } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Funcoes();
                                } else {
                                    addErro(atual(), "'}'");
                                    Funcoes();
                                }
                            }
                        } else {
                            addErro(atual(), "'='");
                            while (!(atual().getLexema().equals("}")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getTipo().equals("string")
                                    || atual().getTipo().equals("float")
                                    || atual().getTipo().equals("int")
                                    || atual().getLexema().equals("true")
                                    || atual().getLexema().equals("false")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else {
                                valores();
                                if ((atual() != null) && (atual().getLexema().equals(";"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                        Funcoes();
                                    } else {
                                        addErro(atual(), "'}'");
                                        Funcoes();
                                    }

                                } else {
                                    addErro(atual(), "';'");
                                    while (!(atual().getLexema().equals("}")
                                            || atual().getLexema().equals("function")
                                            || atual().getLexema().equals("procedure")
                                            || atual().getLexema().equals("$"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    }
                                    if ((atual().getLexema().equals("$"))) {
                                        addErro(atual(), "fim de problema");
                                    } else if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                        Funcoes();
                                    } else {
                                        addErro(atual(), "'}'");
                                        Funcoes();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // <Parametros> ::= Tipo Identificador <MaisParametros> | <>
    public void Parametros() {

        if ((atual() != null) && (tipos.contains(atual().getLexema()))) {

            auxvar = atual();
            tabela.addvar();
            tabela.setTipovar(atual());

            posicaoAtual = posicaoAtual + 1;
            if ((atual() != null) && (atual().getTipo().equals("IDE"))) {

                tabela.setIdevar(atual());
                tabela.addListavar();
                tabela.addParametroFun();

                posicaoAtual = posicaoAtual + 1;
                MaisParametros();
            } else {
                addErro(atual(), "IDE");
                while (!(atual().getLexema().equals(")")
                        || atual().getLexema().equals("{")
                        || atual().getLexema().equals("}")
                        || atual().getLexema().equals("$"))) {
                    posicaoAtual = posicaoAtual + 1;
                }
                if ((atual().getLexema().equals("$"))) {
                    addErro(atual(), "fim de problema");
                }

            }
        }
    }
    // <MaisParametros> ::= ',' Identificador <MaisParametros> | ';' <Parametros> |
    // <>

    public void MaisParametros() {
        if ((atual() != null) && (atual().getLexema().equals(","))) {
            posicaoAtual = posicaoAtual + 1;
            if ((atual() != null) && (atual().getTipo().equals("IDE"))) {

                tabela.addvar();
                tabela.setTipovar(auxvar);
                tabela.setIdevar(atual());
                tabela.addListavar();
                tabela.addParametroFun();

                posicaoAtual = posicaoAtual + 1;
                MaisParametros();
            } else {
                addErro(atual(), "IDE");
                while (!(atual().getLexema().equals(")")
                        || atual().getLexema().equals("{")
                        || atual().getLexema().equals("}")
                        || atual().getLexema().equals("$"))) {
                    posicaoAtual = posicaoAtual + 1;
                }
                if ((atual().getLexema().equals("$"))) {
                    addErro(atual(), "fim de problema");
                }
            }
        } else if ((atual() != null) && (atual().getLexema().equals(";"))) {
            posicaoAtual = posicaoAtual + 1;
            Parametros();
        }
    }

    // <Comandos> ::= <Write> <Comandos> | <Read> <Comandos> | <For> <Comandos> |
    // <If> <Comandos> | <Atribuicao> <Comandos> | <>
    public void Comandos() {
        if ((atual() != null) && (atual().getLexema().equals("write"))) {
            Write();
        } else if ((atual() != null) && (atual().getLexema().equals("read"))) {
            Read();
        } else if ((atual() != null) && (atual().getLexema().equals("for"))) {
            For();
        } else if ((atual() != null) && (atual().getLexema().equals("if"))) {
            If();
        } else if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
            Atribuicao();

        }

    }

    // <Write> ::= write '(' <Imprimir> ')' ';'
    public void Write() {
        if ((atual() != null) && (atual().getLexema().equals("write"))) {
            posicaoAtual = posicaoAtual + 1;
            if ((atual() != null) && (atual().getLexema().equals("("))) {
                posicaoAtual = posicaoAtual + 1;
                imprimirWrite();
                if ((atual() != null) && (atual().getLexema().equals(")"))) {
                    posicaoAtual = posicaoAtual + 1;
                    if ((atual() != null) && (atual().getLexema().equals(";"))) {
                        posicaoAtual = posicaoAtual + 1;
                        Comandos();
                    } else {
                        addErro(atual(), "';'");
                        while (!(atual().getLexema().equals("write")
                                || atual().getLexema().equals("read")
                                || atual().getLexema().equals("for")
                                || atual().getLexema().equals("$")
                                || atual().getLexema().equals(".")
                                || atual().getTipo().equals("IDE")
                                || atual().getLexema().equals("if"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if ((atual().getLexema().equals("$"))) {
                            addErro(atual(), "fim de problema");
                        } else {
                            Comandos();
                        }
                    }
                } else {
                    addErro(atual(), "')'");
                    while (!(atual().getLexema().equals(";")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "fim de programa");
                    } else {
                        if ((atual() != null) && (atual().getLexema().equals(";"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Comandos();
                        } else {
                            addErro(atual(), "';'");
                            while (!(atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("$")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals(".")
                                    || atual().getLexema().equals("if"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else {
                                Comandos();
                            }
                        }
                    }
                }
            } else {
                addErro(atual(), "'('");
                while (!(atual().getLexema().equals(")")
                        || atual().getTipo().equals("string")
                        || atual().getTipo().equals("IDE")
                        || atual().getLexema().equals("$"))) {
                    posicaoAtual = posicaoAtual + 1;
                }
                if (atual().getLexema().equals("$")) {
                    addErro(atual(), "fim de programa");
                } else {
                    imprimirWrite();
                    if ((atual() != null) && (atual().getLexema().equals(")"))) {
                        posicaoAtual = posicaoAtual + 1;
                        if ((atual() != null) && (atual().getLexema().equals(";"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Comandos();
                        } else {
                            addErro(atual(), "';'");
                            while (!(atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("$")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals(".")
                                    || atual().getLexema().equals("if"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else {
                                Comandos();
                            }
                        }
                    } else {
                        addErro(atual(), "')'");
                        while (!(atual().getLexema().equals(";")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Comandos();
                        }
                        if (atual().getLexema().equals("$")) {
                            addErro(atual(), "fim de programa");
                        } else {
                            if ((atual() != null) && (atual().getLexema().equals(";"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Comandos();
                            } else {
                                addErro(atual(), "';'");
                                while (!(atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("for")
                                        || atual().getLexema().equals("$")
                                        || atual().getLexema().equals(".")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getLexema().equals("if"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if ((atual().getLexema().equals("$"))) {
                                    addErro(atual(), "fim de problema");
                                } else {
                                    Comandos();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    // <Imprimir> ::= Identificador <Matriz> | Cadeia

    public void imprimirWrite() {
        if ((atual() != null) && (atual().getTipo().equals("string"))) {
            posicaoAtual = posicaoAtual + 1;
        } else if ((atual() != null) && (atual().getTipo().equals("IDE"))) {

            tabela.VerificarIdeDeclarado(atual());

            posicaoAtual = posicaoAtual + 1;
            if (atual().getLexema().equals("[")) {
                Matriz();
            }
        } else if ((atual() != null) && (atual().getLexema().equals(")"))) {
            addErro(atual(), "Valor inválido");
        } else {
            addErro(atual(), "Valor inválido");
            while (!(atual().getLexema().equals(")")
                    || atual().getLexema().equals(";")
                    || atual().getLexema().equals("$"))) {
                posicaoAtual = posicaoAtual + 1;
            }
            if (atual().getLexema().equals("$")) {
                addErro(atual(), "fim de programa");
            }
        }
    }
    // <Read> ::= read '(' Identificador <Matriz> ')' ';'

    public void Read() {
        if ((atual() != null) && (atual().getLexema().equals("read"))) {
            posicaoAtual = posicaoAtual + 1;
            if ((atual() != null) && (atual().getLexema().equals("("))) {
                posicaoAtual = posicaoAtual + 1;
                imprimirRead();
                if ((atual() != null) && (atual().getLexema().equals(")"))) {
                    posicaoAtual = posicaoAtual + 1;
                    if ((atual() != null) && (atual().getLexema().equals(";"))) {
                        posicaoAtual = posicaoAtual + 1;
                        Comandos();
                    } else {
                        addErro(atual(), "';'");
                        while (!(atual().getLexema().equals("write")
                                || atual().getLexema().equals("read")
                                || atual().getLexema().equals("for")
                                || atual().getLexema().equals("$")
                                || atual().getLexema().equals(".")
                                || atual().getTipo().equals("IDE")
                                || atual().getLexema().equals("if"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if ((atual().getLexema().equals("$"))) {
                            addErro(atual(), "fim de problema");
                        } else {
                            Comandos();
                        }
                    }
                } else {
                    addErro(atual(), "')'");
                    while (!(atual().getLexema().equals(";")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                        Comandos();
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "fim de programa");
                    } else {
                        if ((atual() != null) && (atual().getLexema().equals(";"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Comandos();
                        } else {
                            addErro(atual(), "';'");
                            while (!(atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("$")
                                    || atual().getLexema().equals(".")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else {
                                Comandos();
                            }
                        }
                    }
                }
            } else {
                addErro(atual(), "'('");
                while (!(atual().getLexema().equals(")")
                        || atual().getTipo().equals("IDE")
                        || atual().getLexema().equals("$"))) {
                    posicaoAtual = posicaoAtual + 1;
                }
                if (atual().getLexema().equals("$")) {
                    addErro(atual(), "fim de programa");
                } else {
                    imprimirRead();
                    if ((atual() != null) && (atual().getLexema().equals(")"))) {
                        posicaoAtual = posicaoAtual + 1;
                        if ((atual() != null) && (atual().getLexema().equals(";"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Comandos();
                        } else {
                            addErro(atual(), "';'");
                            while (!(atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("$")
                                    || atual().getLexema().equals(".")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else {
                                Comandos();
                            }
                        }
                    } else {
                        addErro(atual(), "')'");
                        while (!(atual().getLexema().equals(";")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Comandos();
                        }
                        if (atual().getLexema().equals("$")) {
                            addErro(atual(), "fim de programa");
                        } else {
                            if ((atual() != null) && (atual().getLexema().equals(";"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Comandos();
                            } else {
                                addErro(atual(), "';'");
                                while (!(atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("for")
                                        || atual().getLexema().equals("$")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getLexema().equals(".")
                                        || atual().getLexema().equals("if"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if ((atual().getLexema().equals("$"))) {
                                    addErro(atual(), "fim de problema");
                                } else {
                                    Comandos();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void imprimirRead() {
        if ((atual() != null) && (atual().getTipo().equals("IDE"))) {

            tabela.VerificarIdeDeclarado(atual());

            posicaoAtual = posicaoAtual + 1;
            if (atual().getLexema().equals("[")) {
                Matriz();
            }
        } else if ((atual() != null) && (atual().getLexema().equals(")"))) {
            addErro(atual(), "Valor inválido");
        } else {
            addErro(atual(), "Valor inválido");
            while (!(atual().getLexema().equals(")")
                    || atual().getLexema().equals(";")
                    || atual().getLexema().equals(".")
                    || atual().getLexema().equals("$"))) {
                posicaoAtual = posicaoAtual + 1;
            }
            if (atual().getLexema().equals("$")) {
                addErro(atual(), "fim de programa");
            }
        }
    }

    // <For> ::= for '(' Identificador '=' <ContadorFor> ';' <ExpCondicional> ';'
    // Identificador Incremento')' '{' <Comandos> '}'
    public void For() {
        if ((atual() != null) && (atual().getLexema().equals("for"))) {
            posicaoAtual = posicaoAtual + 1;
            if ((atual() != null) && (atual().getLexema().equals("("))) {
                posicaoAtual = posicaoAtual + 1;
                CondicaoFor();
                if ((atual() != null) && (atual().getLexema().equals(")"))) {
                    posicaoAtual = posicaoAtual + 1;
                    if ((atual() != null) && (atual().getLexema().equals("{"))) {
                        posicaoAtual = posicaoAtual + 1;
                        Comandos();
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Comandos();
                        } else {
                            addErro(atual(), "'}'");
                            while (!(atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("$")
                                    || atual().getLexema().equals(".")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else {
                                Comandos();
                            }
                        }
                    } else {
                        addErro(atual(), "'{'");
                        while (!(atual().getLexema().equals("write")
                                || atual().getLexema().equals("read")
                                || atual().getLexema().equals("if")
                                || atual().getLexema().equals("for")
                                || atual().getTipo().equals("IDE")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if (!(atual().getLexema().equals("$"))) {
                            Comandos();
                        } else {
                            addErro(atual(), "fim de programa");
                            return;
                        }
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                        } else {
                            addErro(atual(), "'}'");
                            while (!(atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("$")
                                    || atual().getLexema().equals(".")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else {
                                Comandos();
                            }
                        }
                    }
                } else {
                    addErro(atual(), "')'");
                    /**/ while (!(atual().getLexema().equals("{")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if ((atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                        addErro(atual(), "fim de programa");
                    } else if ((atual() != null) && (atual().getLexema().equals("{"))) {
                        posicaoAtual = posicaoAtual + 1;
                        Comandos();
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Comandos();
                        } else {
                            addErro(atual(), "'}'");
                            while (!(atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("$")
                                    || atual().getLexema().equals(".")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else {
                                Comandos();
                            }
                        }
                    } else {
                        addErro(atual(), "'{'");
                        while (!(atual().getLexema().equals("write")
                                || atual().getLexema().equals("read")
                                || atual().getLexema().equals("if")
                                || atual().getLexema().equals("for")
                                || atual().getTipo().equals("IDE")
                                || atual().getLexema().equals("else")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if (!(atual().getLexema().equals("$"))) {
                            Comandos();
                        } else {
                            addErro(atual(), "fim de programa");
                            return;
                        }
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Comandos();
                        } else {
                            addErro(atual(), "'}'");
                            while (!(atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("$")
                                    || atual().getLexema().equals(".")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else {
                                Comandos();
                            }
                        }
                    }

                }
            } else {
                addErro(atual(), "'('");
                if ((atual() != null) && !(atual().getTipo().equals("IDE"))) {
                    while (!(atual().getTipo().equals("IDE")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                } else if (!(atual().getLexema().equals("$"))) {
                    CondicaoFor();
                } else {
                    addErro(atual(), "fim de programa");
                    return;
                }
                if ((atual() != null) && (atual().getLexema().equals(")"))) {
                    posicaoAtual = posicaoAtual + 1;
                    if ((atual() != null) && (atual().getLexema().equals("{"))) {
                        posicaoAtual = posicaoAtual + 1;
                        Comandos();
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Comandos();
                        } else {
                            addErro(atual(), "'}'");
                            while (!(atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("$")
                                    || atual().getLexema().equals(".")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else {
                                Comandos();
                            }
                        }
                    } else {
                        addErro(atual(), "'{'");
                        while (!(atual().getLexema().equals("write")
                                || atual().getLexema().equals("read")
                                || atual().getLexema().equals("if")
                                || atual().getLexema().equals("for")
                                || atual().getTipo().equals("IDE")
                                || atual().getLexema().equals("else")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if (!(atual().getLexema().equals("$"))) {
                            Comandos();
                        } else {
                            addErro(atual(), "fim de programa");
                            return;
                        }
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Comandos();
                        } else {
                            addErro(atual(), "'}'");
                            while (!(atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("$")
                                    || atual().getLexema().equals(".")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else {
                                Comandos();
                            }
                        }
                    }
                } else {
                    addErro(atual(), "')'");
                    /**/ while (!(atual().getLexema().equals("{")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if ((atual().getLexema().equals("$"))) {
                        addErro(atual(), "fim de programa");
                    } else if ((atual() != null) && (atual().getLexema().equals("{"))) {
                        posicaoAtual = posicaoAtual + 1;
                        Comandos();
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Comandos();
                        } else {
                            addErro(atual(), "'}'");
                            while (!(atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals(".")
                                    || atual().getLexema().equals("$")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else {
                                Comandos();
                            }
                        }
                    } else {
                        addErro(atual(), "'{'");
                        while (!(atual().getLexema().equals("write")
                                || atual().getLexema().equals("read")
                                || atual().getLexema().equals("if")
                                || atual().getLexema().equals(".")
                                || atual().getLexema().equals("for")
                                || atual().getTipo().equals("IDE")
                                || atual().getLexema().equals("else")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if (!(atual().getLexema().equals("$"))) {
                            Comandos();
                        } else {
                            addErro(atual(), "fim de programa");
                            return;
                        }
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Comandos();
                        } else {
                            addErro(atual(), "'}'");
                            while (!(atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals("$")
                                    || atual().getLexema().equals(".")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("if"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if ((atual().getLexema().equals("$"))) {
                                addErro(atual(), "fim de problema");
                            } else {
                                Comandos();
                            }
                        }
                    }

                }
            }
        }
    }

    // Identificador '=' <ContadorFor> ';' <ExpCondicional> ';' Identificador
    // Incremento
    public void CondicaoFor() {

        Token aux_for, aux_for_1;

        if ((atual() != null) && (atual().getTipo().equals("IDE"))) {

            aux_for = atual();
            tabela.VerificarIdeDeclarado(atual());
            tabela.verificaInteiro(atual());

            posicaoAtual = posicaoAtual + 1;
            if ((atual() != null) && (atual().getLexema().equals("="))) {
                posicaoAtual = posicaoAtual + 1;

                aux_for_1 = atual();
                tabela.condicaoFor(aux_for, aux_for_1);

                ContadorFor();
                if ((atual() != null) && (atual().getLexema().equals(";"))) {
                    posicaoAtual = posicaoAtual + 1;
                    aux_tipo = atual();
                    ExpCondicional();
                    if ((atual() != null) && (atual().getLexema().equals(";"))) {
                        posicaoAtual = posicaoAtual + 1;
                        if ((atual() != null) && (atual().getTipo().equals("IDE"))) {

                            tabela.verificaInteiro(atual());

                            posicaoAtual = posicaoAtual + 1;
                            if ((atual() != null) && ((atual().getLexema().equals("++")
                                    || (atual().getLexema().equals("--"))))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "Incremento");
                            }
                        } else {
                            addErro(atual(), "IDE");
                            while (!(atual().getLexema().equals("++")
                                    || atual().getLexema().equals("$")
                                    || atual().getLexema().equals("--"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            } else {
                                if ((atual() != null) && ((atual().getLexema().equals("++")
                                        || (atual().getLexema().equals("--"))))) {
                                    posicaoAtual = posicaoAtual + 1;
                                } else {
                                    addErro(atual(), "Incremento");
                                }
                            }
                        }
                    } else {
                        addErro(atual(), "';'");

                        if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                            posicaoAtual = posicaoAtual + 1;
                            if ((atual() != null) && ((atual().getLexema().equals("++")
                                    || (atual().getLexema().equals("--"))))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "Incremento");
                            }
                        } else {
                            addErro(atual(), "IDE");
                            while (!(atual().getLexema().equals("++")
                                    || atual().getLexema().equals("$")
                                    || atual().getLexema().equals("--"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            } else {
                                if ((atual() != null) && ((atual().getLexema().equals("++")
                                        || (atual().getLexema().equals("--"))))) {
                                    posicaoAtual = posicaoAtual + 1;
                                } else {
                                    addErro(atual(), "Incremento");
                                }
                            }
                        }

                    }
                } else {
                    addErro(atual(), "';'");
                    while (!(atual().getLexema().equals("(")
                            || atual().getLexema().equals("$")
                            || atual().getLexema().equals("!")
                            || atual().getTipo().equals("IDE")
                            || atual().getTipo().equals("int")
                            || atual().getTipo().equals("float"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "fim de programa");
                    } else {
                        ExpCondicional();
                        if ((atual() != null) && (atual().getLexema().equals(";"))) {
                            posicaoAtual = posicaoAtual + 1;
                            if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && ((atual().getLexema().equals("++")
                                        || (atual().getLexema().equals("--"))))) {
                                    posicaoAtual = posicaoAtual + 1;
                                } else {
                                    addErro(atual(), "Incremento");
                                }
                            } else {
                                addErro(atual(), "IDE");
                                while (!(atual().getLexema().equals("++")
                                        || atual().getLexema().equals("$")
                                        || atual().getLexema().equals("--"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                } else {
                                    if ((atual() != null) && ((atual().getLexema().equals("++")
                                            || (atual().getLexema().equals("--"))))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    } else {
                                        addErro(atual(), "Incremento");
                                    }
                                }
                            }
                        } else {
                            addErro(atual(), "';'");

                            if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && ((atual().getLexema().equals("++")
                                        || (atual().getLexema().equals("--"))))) {
                                    posicaoAtual = posicaoAtual + 1;
                                } else {
                                    addErro(atual(), "Incremento");
                                }
                            } else {
                                addErro(atual(), "IDE");
                                while (!(atual().getLexema().equals("++")
                                        || atual().getLexema().equals("$")
                                        || atual().getLexema().equals("--"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                } else {
                                    if ((atual() != null) && ((atual().getLexema().equals("++")
                                            || (atual().getLexema().equals("--"))))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    } else {
                                        addErro(atual(), "Incremento");
                                    }
                                }
                            }

                        }
                    }
                }
            } else {
                addErro(atual(), "'='");
                while (!(atual().getLexema().equals(";")
                        || atual().getLexema().equals("$")
                        || atual().getTipo().equals("int")
                        || atual().getTipo().equals("IDE"))) {
                    posicaoAtual = posicaoAtual + 1;
                }
                if (atual().getLexema().equals("$")) {
                    addErro(atual(), "fim de programa");
                } else {
                    ContadorFor();
                    if ((atual() != null) && (atual().getLexema().equals(";"))) {
                        posicaoAtual = posicaoAtual + 1;
                        ExpCondicional();
                        if ((atual() != null) && (atual().getLexema().equals(";"))) {
                            posicaoAtual = posicaoAtual + 1;
                            if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && ((atual().getLexema().equals("++")
                                        || (atual().getLexema().equals("--"))))) {
                                    posicaoAtual = posicaoAtual + 1;
                                } else {
                                    addErro(atual(), "Incremento");
                                }
                            } else {
                                addErro(atual(), "IDE");
                                while (!(atual().getLexema().equals("++")
                                        || atual().getLexema().equals("$")
                                        || atual().getLexema().equals("--"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                } else {
                                    if ((atual() != null) && ((atual().getLexema().equals("++")
                                            || (atual().getLexema().equals("--"))))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    } else {
                                        addErro(atual(), "Incremento");
                                    }
                                }
                            }
                        } else {
                            addErro(atual(), "';'");

                            if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && ((atual().getLexema().equals("++")
                                        || (atual().getLexema().equals("--"))))) {
                                    posicaoAtual = posicaoAtual + 1;
                                } else {
                                    addErro(atual(), "Incremento");
                                }
                            } else {
                                addErro(atual(), "IDE");
                                while (!(atual().getLexema().equals("++")
                                        || atual().getLexema().equals("$")
                                        || atual().getLexema().equals("--"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                } else {
                                    if ((atual() != null) && ((atual().getLexema().equals("++")
                                            || (atual().getLexema().equals("--"))))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    } else {
                                        addErro(atual(), "Incremento");
                                    }
                                }
                            }

                        }
                    } else {
                        addErro(atual(), "';'");
                        while (!(atual().getLexema().equals("(")
                                || atual().getLexema().equals("$")
                                || atual().getLexema().equals("!")
                                || atual().getTipo().equals("IDE")
                                || atual().getTipo().equals("int")
                                || atual().getTipo().equals("float"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if (atual().getLexema().equals("$")) {
                            addErro(atual(), "fim de programa");
                        } else {
                            ExpCondicional();
                            if ((atual() != null) && (atual().getLexema().equals(";"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    if ((atual() != null) && ((atual().getLexema().equals("++")
                                            || (atual().getLexema().equals("--"))))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    } else {
                                        addErro(atual(), "Incremento");
                                    }
                                } else {
                                    addErro(atual(), "IDE");
                                    while (!(atual().getLexema().equals("++")
                                            || atual().getLexema().equals("$")
                                            || atual().getLexema().equals("--"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    }
                                    if (atual().getLexema().equals("$")) {
                                        addErro(atual(), "fim de programa");
                                    } else {
                                        if ((atual() != null) && ((atual().getLexema().equals("++")
                                                || (atual().getLexema().equals("--"))))) {
                                            posicaoAtual = posicaoAtual + 1;
                                        } else {
                                            addErro(atual(), "Incremento");
                                        }
                                    }
                                }
                            } else {
                                addErro(atual(), "';'");

                                if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    if ((atual() != null) && ((atual().getLexema().equals("++")
                                            || (atual().getLexema().equals("--"))))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    } else {
                                        addErro(atual(), "Incremento");
                                    }
                                } else {
                                    addErro(atual(), "IDE");
                                    while (!(atual().getLexema().equals("++")
                                            || atual().getLexema().equals("$")
                                            || atual().getLexema().equals("--"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    }
                                    if (atual().getLexema().equals("$")) {
                                        addErro(atual(), "fim de programa");
                                    } else {
                                        if ((atual() != null) && ((atual().getLexema().equals("++")
                                                || (atual().getLexema().equals("--"))))) {
                                            posicaoAtual = posicaoAtual + 1;
                                        } else {
                                            addErro(atual(), "Incremento");
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        } else {
            addErro(atual(), "IDE");
            while (!(atual().getLexema().equals("=")
                    || atual().getLexema().equals(";")
                    || atual().getLexema().equals("$"))) {
                posicaoAtual = posicaoAtual + 1;
            }
            if (atual().getLexema().equals("$")) {
                addErro(atual(), "Fim de programa");
                return;
            } else if ((atual() != null) && (atual().getLexema().equals("="))) {
                posicaoAtual = posicaoAtual + 1;
                ContadorFor();
                if ((atual() != null) && (atual().getLexema().equals(";"))) {
                    posicaoAtual = posicaoAtual + 1;
                    ExpCondicional();
                    if ((atual() != null) && (atual().getLexema().equals(";"))) {
                        posicaoAtual = posicaoAtual + 1;
                        if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                            posicaoAtual = posicaoAtual + 1;
                            if ((atual() != null) && ((atual().getLexema().equals("++")
                                    || (atual().getLexema().equals("--"))))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "Incremento");
                            }
                        } else {
                            addErro(atual(), "IDE");
                            while (!(atual().getLexema().equals("++")
                                    || atual().getLexema().equals("$")
                                    || atual().getLexema().equals("--"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            } else {
                                if ((atual() != null) && ((atual().getLexema().equals("++")
                                        || (atual().getLexema().equals("--"))))) {
                                    posicaoAtual = posicaoAtual + 1;
                                } else {
                                    addErro(atual(), "Incremento");
                                }
                            }
                        }
                    } else {
                        addErro(atual(), "';'");

                        if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                            posicaoAtual = posicaoAtual + 1;
                            if ((atual() != null) && ((atual().getLexema().equals("++")
                                    || (atual().getLexema().equals("--"))))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "Incremento");
                            }
                        } else {
                            addErro(atual(), "IDE");
                            while (!(atual().getLexema().equals("++")
                                    || atual().getLexema().equals("$")
                                    || atual().getLexema().equals("--"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            } else {
                                if ((atual() != null) && ((atual().getLexema().equals("++")
                                        || (atual().getLexema().equals("--"))))) {
                                    posicaoAtual = posicaoAtual + 1;
                                } else {
                                    addErro(atual(), "Incremento");
                                }
                            }
                        }

                    }
                } else {
                    addErro(atual(), "';'");
                    while (!(atual().getLexema().equals("(")
                            || atual().getLexema().equals("$")
                            || atual().getLexema().equals("!")
                            || atual().getTipo().equals("IDE")
                            || atual().getTipo().equals("int")
                            || atual().getTipo().equals("float"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "fim de programa");
                    } else {
                        ExpCondicional();
                        if ((atual() != null) && (atual().getLexema().equals(";"))) {
                            posicaoAtual = posicaoAtual + 1;
                            if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && ((atual().getLexema().equals("++")
                                        || (atual().getLexema().equals("--"))))) {
                                    posicaoAtual = posicaoAtual + 1;
                                } else {
                                    addErro(atual(), "Incremento");
                                }
                            } else {
                                addErro(atual(), "IDE");
                                while (!(atual().getLexema().equals("++")
                                        || atual().getLexema().equals("$")
                                        || atual().getLexema().equals("--"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                } else {
                                    if ((atual() != null) && ((atual().getLexema().equals("++")
                                            || (atual().getLexema().equals("--"))))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    } else {
                                        addErro(atual(), "Incremento");
                                    }
                                }
                            }
                        } else {
                            addErro(atual(), "';'");

                            if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && ((atual().getLexema().equals("++")
                                        || (atual().getLexema().equals("--"))))) {
                                    posicaoAtual = posicaoAtual + 1;
                                } else {
                                    addErro(atual(), "Incremento");
                                }
                            } else {
                                addErro(atual(), "IDE");
                                while (!(atual().getLexema().equals("++")
                                        || atual().getLexema().equals("$")
                                        || atual().getLexema().equals("--"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                } else {
                                    if ((atual() != null) && ((atual().getLexema().equals("++")
                                            || (atual().getLexema().equals("--"))))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    } else {
                                        addErro(atual(), "Incremento");
                                    }
                                }
                            }

                        }
                    }
                }
            } else {
                addErro(atual(), "'='");
                while (!(atual().getLexema().equals(";")
                        || atual().getLexema().equals("$")
                        || atual().getTipo().equals("int")
                        || atual().getTipo().equals("IDE"))) {
                    posicaoAtual = posicaoAtual + 1;
                }
                if (atual().getLexema().equals("$")) {
                    addErro(atual(), "fim de programa");
                } else {
                    ContadorFor();
                    if ((atual() != null) && (atual().getLexema().equals(";"))) {
                        posicaoAtual = posicaoAtual + 1;
                        ExpCondicional();
                        if ((atual() != null) && (atual().getLexema().equals(";"))) {
                            posicaoAtual = posicaoAtual + 1;
                            if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && ((atual().getLexema().equals("++")
                                        || (atual().getLexema().equals("--"))))) {
                                    posicaoAtual = posicaoAtual + 1;
                                } else {
                                    addErro(atual(), "Incremento");
                                }
                            } else {
                                addErro(atual(), "IDE");
                                while (!(atual().getLexema().equals("++")
                                        || atual().getLexema().equals("$")
                                        || atual().getLexema().equals("--"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                } else {
                                    if ((atual() != null) && ((atual().getLexema().equals("++")
                                            || (atual().getLexema().equals("--"))))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    } else {
                                        addErro(atual(), "Incremento");
                                    }
                                }
                            }
                        } else {
                            addErro(atual(), "';'");

                            if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && ((atual().getLexema().equals("++")
                                        || (atual().getLexema().equals("--"))))) {
                                    posicaoAtual = posicaoAtual + 1;
                                } else {
                                    addErro(atual(), "Incremento");
                                }
                            } else {
                                addErro(atual(), "IDE");
                                while (!(atual().getLexema().equals("++")
                                        || atual().getLexema().equals("$")
                                        || atual().getLexema().equals("--"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                } else {
                                    if ((atual() != null) && ((atual().getLexema().equals("++")
                                            || (atual().getLexema().equals("--"))))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    } else {
                                        addErro(atual(), "Incremento");
                                    }
                                }
                            }

                        }
                    } else {
                        addErro(atual(), "';'");
                        while (!(atual().getLexema().equals("(")
                                || atual().getLexema().equals("$")
                                || atual().getLexema().equals("!")
                                || atual().getTipo().equals("IDE")
                                || atual().getTipo().equals("int")
                                || atual().getTipo().equals("float"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if (atual().getLexema().equals("$")) {
                            addErro(atual(), "fim de programa");
                        } else {
                            ExpCondicional();
                            if ((atual() != null) && (atual().getLexema().equals(";"))) {
                                posicaoAtual = posicaoAtual + 1;
                                if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    if ((atual() != null) && ((atual().getLexema().equals("++")
                                            || (atual().getLexema().equals("--"))))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    } else {
                                        addErro(atual(), "Incremento");
                                    }
                                } else {
                                    addErro(atual(), "IDE");
                                    while (!(atual().getLexema().equals("++")
                                            || atual().getLexema().equals("$")
                                            || atual().getLexema().equals("--"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    }
                                    if (atual().getLexema().equals("$")) {
                                        addErro(atual(), "fim de programa");
                                    } else {
                                        if ((atual() != null) && ((atual().getLexema().equals("++")
                                                || (atual().getLexema().equals("--"))))) {
                                            posicaoAtual = posicaoAtual + 1;
                                        } else {
                                            addErro(atual(), "Incremento");
                                        }
                                    }
                                }
                            } else {
                                addErro(atual(), "';'");

                                if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    if ((atual() != null) && ((atual().getLexema().equals("++")
                                            || (atual().getLexema().equals("--"))))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    } else {
                                        addErro(atual(), "Incremento");
                                    }
                                } else {
                                    addErro(atual(), "IDE");
                                    while (!(atual().getLexema().equals("++")
                                            || atual().getLexema().equals("$")
                                            || atual().getLexema().equals("--"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    }
                                    if (atual().getLexema().equals("$")) {
                                        addErro(atual(), "fim de programa");
                                    } else {
                                        if ((atual() != null) && ((atual().getLexema().equals("++")
                                                || (atual().getLexema().equals("--"))))) {
                                            posicaoAtual = posicaoAtual + 1;
                                        } else {
                                            addErro(atual(), "Incremento");
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }
    // <ContadorFor> ::= Identitificador | int

    public void ContadorFor() {
        if ((atual() != null) && ((atual().getTipo().equals("IDE"))
                || (atual().getTipo().equals("int")))) {
            posicaoAtual = posicaoAtual + 1;
        } else {
            addErro(atual(), "ContadorFor");
            while (!(atual().getLexema().equals(";")
                    || atual().getLexema().equals(".")
                    || atual().getLexema().equals("$"))) {
                posicaoAtual = posicaoAtual + 1;
            }
            if ((atual().getLexema().equals("$"))) {
                addErro(atual(), "fim de problema");

            }
        }
    }
    // <If> ::= 'if' '(' <ExpLogica> ')' '{' <Comandos> '}' <Else>

    public void If() {
        if ((atual() != null) && (atual().getLexema().equals("if"))) {
            posicaoAtual = posicaoAtual + 1;
            if ((atual() != null) && (atual().getLexema().equals("("))) {
                posicaoAtual = posicaoAtual + 1;
                ExpLogica();
                if ((atual() != null) && (atual().getLexema().equals(")"))) {
                    posicaoAtual = posicaoAtual + 1;
                    if ((atual() != null) && (atual().getLexema().equals("{"))) {
                        posicaoAtual = posicaoAtual + 1;
                        Comandos();
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Else();
                        } else {
                            addErro(atual(), "'}'");
                            while (!(atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("if")
                                    || atual().getLexema().equals(".")
                                    || atual().getLexema().equals("for")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("else")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            } else if (atual().getLexema().equals("else")) {
                                Else();
                            } else {
                                Comandos();
                            }
                        }
                    } else {
                        addErro(atual(), "'{'");
                        while (!(atual().getLexema().equals("write")
                                || atual().getLexema().equals("read")
                                || atual().getLexema().equals("if")
                                || atual().getLexema().equals("for")
                                || atual().getLexema().equals(".")
                                || atual().getTipo().equals("IDE")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if (atual().getLexema().equals("$")) {
                            addErro(atual(), "fim de programa");
                        } else {
                            Comandos();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Else();
                            } else {
                                addErro(atual(), "'}'");
                                while (!(atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("for")
                                        || atual().getLexema().equals(".")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getLexema().equals("else")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                } else if (atual().getLexema().equals("else")) {
                                    Else();
                                } else {
                                    Comandos();
                                }
                            }
                        }
                    }
                } else {
                    addErro(atual(), "')'");
                    if ((atual() != null) && (atual().getLexema().equals("{"))) {
                        posicaoAtual = posicaoAtual + 1;
                        Comandos();
                        if ((atual() != null) && (atual().getLexema().equals("}"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Else();
                        } else {
                            addErro(atual(), "'}'");
                            while (!(atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("if")
                                    || atual().getLexema().equals("for")
                                    || atual().getLexema().equals(".")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("else")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            } else if (atual().getLexema().equals("else")) {
                                Else();
                            } else {
                                Comandos();
                            }
                        }
                    } else {
                        addErro(atual(), "'{'");
                        while (!(atual().getLexema().equals("write")
                                || atual().getLexema().equals("read")
                                || atual().getLexema().equals("if")
                                || atual().getLexema().equals("for")
                                || atual().getLexema().equals(".")
                                || atual().getTipo().equals("IDE")
                                || atual().getLexema().equals("$"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if (atual().getLexema().equals("$")) {
                            addErro(atual(), "fim de programa");
                        } else {
                            Comandos();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Else();
                            } else {
                                while (!(atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("for")
                                        || atual().getLexema().equals(".")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getLexema().equals("else")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                } else if (atual().getLexema().equals("else")) {
                                    Else();
                                } else {
                                    Comandos();
                                }
                            }
                        }
                    }
                }
            } else {
                addErro(atual(), "'('");
                while (!(atual().getTipo().equals("int")
                        || atual().getTipo().equals("float")
                        || atual().getLexema().equals("(")
                        || atual().getLexema().equals("!")
                        || atual().getTipo().equals("IDE")
                        || atual().getLexema().equals("$"))) {
                    posicaoAtual = posicaoAtual + 1;
                }
                if (atual().getLexema().equals("$")) {
                    addErro(atual(), "fim de programa");
                } else {
                    ExpLogica();
                    if ((atual() != null) && (atual().getLexema().equals(")"))) {
                        posicaoAtual = posicaoAtual + 1;
                        if ((atual() != null) && (atual().getLexema().equals("{"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Comandos();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Else();
                            } else {
                                addErro(atual(), "'}'");
                                while (!(atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("for")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getLexema().equals(".")
                                        || atual().getLexema().equals("else")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                } else if (atual().getLexema().equals("else")) {
                                    Else();
                                } else {
                                    Comandos();
                                }
                            }
                        } else {
                            addErro(atual(), "'{'");
                            while (!(atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("if")
                                    || atual().getLexema().equals("for")
                                    || atual().getTipo().equals("IDE")
                                    || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            } else {
                                Comandos();
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Else();
                                } else {
                                    addErro(atual(), "'}'");
                                    while (!(atual().getLexema().equals("write")
                                            || atual().getLexema().equals("read")
                                            || atual().getLexema().equals("if")
                                            || atual().getLexema().equals("for")
                                            || atual().getTipo().equals("IDE")
                                            || atual().getLexema().equals(".")
                                            || atual().getLexema().equals("else")
                                            || atual().getLexema().equals("$"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    }
                                    if (atual().getLexema().equals("$")) {
                                        addErro(atual(), "fim de programa");
                                    } else if (atual().getLexema().equals("else")) {
                                        Else();
                                    } else {
                                        Comandos();
                                    }
                                }
                            }
                        }
                    } else {
                        addErro(atual(), "')'");
                        if ((atual() != null) && (atual().getLexema().equals("{"))) {
                            posicaoAtual = posicaoAtual + 1;
                            Comandos();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Else();
                            } else {
                                addErro(atual(), "'}'");
                                while (!(atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("if")
                                        || atual().getLexema().equals("for")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getLexema().equals(".")
                                        || atual().getLexema().equals("else")
                                        || atual().getLexema().equals("$"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if (atual().getLexema().equals("$")) {
                                    addErro(atual(), "fim de programa");
                                } else if (atual().getLexema().equals("else")) {
                                    Else();
                                } else {
                                    Comandos();
                                }
                            }
                        } else {
                            addErro(atual(), "'{'");
                            while (!(atual().getLexema().equals("write")
                                    || atual().getLexema().equals("read")
                                    || atual().getLexema().equals("if")
                                    || atual().getLexema().equals("for")
                                    || atual().getTipo().equals("IDE") || atual().getLexema().equals("$"))) {
                                posicaoAtual = posicaoAtual + 1;
                            }
                            if (atual().getLexema().equals("$")) {
                                addErro(atual(), "fim de programa");
                            } else {
                                Comandos();
                                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                    Else();
                                } else {
                                    addErro(atual(), "'}'");
                                    while (!(atual().getLexema().equals("write")
                                            || atual().getLexema().equals("read")
                                            || atual().getLexema().equals("if")
                                            || atual().getLexema().equals("for")
                                            || atual().getTipo().equals("IDE")
                                            || atual().getLexema().equals(".")
                                            || atual().getLexema().equals("else")
                                            || atual().getLexema().equals("$"))) {
                                        posicaoAtual = posicaoAtual + 1;
                                    }
                                    if (atual().getLexema().equals("$")) {
                                        addErro(atual(), "fim de programa");
                                    } else if (atual().getLexema().equals("else")) {
                                        Else();
                                    } else {
                                        Comandos();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    // <Else> ::= 'else' '{' <Comandos> '}' | <>

    public void Else() {
        if ((atual() != null) && (atual().getLexema().equals("else"))) {
            posicaoAtual = posicaoAtual + 1;
            if ((atual() != null) && (atual().getLexema().equals("{"))) {
                posicaoAtual = posicaoAtual + 1;
                Comandos();
                if ((atual() != null) && (atual().getLexema().equals("}"))) {
                    posicaoAtual = posicaoAtual + 1;
                    Comandos();
                } else {
                    addErro(atual(), "'}'");
                    while (!(atual().getLexema().equals("write")
                            || atual().getLexema().equals("read")
                            || atual().getLexema().equals("for")
                            || atual().getLexema().equals("$")
                            || atual().getTipo().equals("IDE")
                            || atual().getLexema().equals(".")
                            || atual().getLexema().equals("if"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if ((atual().getLexema().equals("$"))) {
                        addErro(atual(), "fim de problema");
                    } else {
                        Comandos();
                    }
                }
            } else {
                addErro(atual(), "'{'");
                if (seguinte().getLexema().equals("{")) {
                    posicaoAtual = posicaoAtual + 1;
                    Comandos();
                    if ((atual() != null) && (atual().getLexema().equals("}"))) {
                        posicaoAtual = posicaoAtual + 1;
                        Comandos();
                    } else {
                        addErro(atual(), "'}'");
                        while (!(atual().getLexema().equals("write")
                                || atual().getLexema().equals("read")
                                || atual().getLexema().equals("for")
                                || atual().getLexema().equals(".")
                                || atual().getLexema().equals("$")
                                || atual().getTipo().equals("IDE")
                                || atual().getLexema().equals("if"))) {
                            posicaoAtual = posicaoAtual + 1;
                        }
                        if ((atual().getLexema().equals("$"))) {
                            addErro(atual(), "fim de problema");
                        } else {
                            Comandos();
                        }
                    }
                } else {
                    while (!(atual().getLexema().equals("{")
                            || atual().getLexema().equals("}")
                            || atual().getLexema().equals("write")
                            || atual().getLexema().equals("read")
                            || atual().getLexema().equals("if")
                            || atual().getLexema().equals("for")
                            || atual().getTipo().equals("IDE")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        posicaoAtual = posicaoAtual + 1;
                    } else {
                        if (atual().getLexema().equals("{")) {
                            posicaoAtual = posicaoAtual + 1;
                            Comandos();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                                Comandos();
                            } else {
                                addErro(atual(), "'}'");
                                while (!(atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("for")
                                        || atual().getLexema().equals("$")
                                        || atual().getLexema().equals(".")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getLexema().equals("if"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if ((atual().getLexema().equals("$"))) {
                                    addErro(atual(), "fim de problema");
                                } else {
                                    Comandos();
                                }
                            }
                        } else if (atual().getLexema().equals("}")) {
                            posicaoAtual = posicaoAtual + 1;
                            Comandos();
                        } else {
                            Comandos();
                            if ((atual() != null) && (atual().getLexema().equals("}"))) {
                                posicaoAtual = posicaoAtual + 1;
                            } else {
                                addErro(atual(), "'}'");
                                while (!(atual().getLexema().equals("write")
                                        || atual().getLexema().equals("read")
                                        || atual().getLexema().equals("for")
                                        || atual().getLexema().equals("$")
                                        || atual().getLexema().equals(".")
                                        || atual().getTipo().equals("IDE")
                                        || atual().getLexema().equals("if"))) {
                                    posicaoAtual = posicaoAtual + 1;
                                }
                                if ((atual().getLexema().equals("$"))) {
                                    addErro(atual(), "fim de problema");
                                } else {
                                    Comandos();
                                }
                            }
                        }
                    }
                }

            }
        } else {
            Comandos();
        }

    }
    // <Atribuicao> ::= Identificador <Matriz> '=' <AtribuicaoValores> ';'

    public void Atribuicao() {
        if ((atual() != null) && (atual().getTipo().equals("IDE"))) {

            aux_tipo = atual();
            tabela.VerificarIdeDeclarado(atual());

            posicaoAtual = posicaoAtual + 1;
            if (atual().getLexema().equals("[")) {
                Matriz();
            }
            if ((atual() != null) && (atual().getLexema().equals("="))) {
                posicaoAtual = posicaoAtual + 1;
                AtribuicaoValores();
                if ((atual() != null) && (atual().getLexema().equals(";"))) {
                    posicaoAtual = posicaoAtual + 1;
                    Comandos();
                } else {
                    addErro(atual(), "';'");
                    while (!(atual().getLexema().equals("write")
                            || atual().getLexema().equals("read")
                            || atual().getLexema().equals("for")
                            || atual().getLexema().equals("$")
                            || atual().getLexema().equals(".")
                            || atual().getTipo().equals("IDE")
                            || atual().getLexema().equals("if"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if ((atual().getLexema().equals("$"))) {
                        addErro(atual(), "fim de problema");
                    } else {
                        Comandos();
                    }
                }
            } else {
                addErro(atual(), "'='");
                if (seguinte().getLexema().equals("=")) {
                    posicaoAtual = posicaoAtual + 1;
                } else {
                    while (!(atual().getLexema().equals("true")
                            || atual().getLexema().equals("false")
                            || atual().getLexema().equals("(")
                            || atual().getTipo().equals("IDE")
                            || atual().getTipo().equals("string")
                            || atual().getTipo().equals("int")
                            || atual().getTipo().equals("float")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "fim de programa");
                    }
                    AtribuicaoValores();
                }
                if ((atual() != null) && (atual().getLexema().equals(";"))) {
                    posicaoAtual = posicaoAtual + 1;
                    Comandos();
                } else {
                    addErro(atual(), "';'");
                    while (!(atual().getLexema().equals("write")
                            || atual().getLexema().equals("read")
                            || atual().getLexema().equals("for")
                            || atual().getLexema().equals(".")
                            || atual().getLexema().equals("$")
                            || atual().getTipo().equals("IDE")
                            || atual().getLexema().equals("if"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if ((atual().getLexema().equals("$"))) {
                        addErro(atual(), "fim de problema");
                    } else {
                        Comandos();
                    }
                }
            }
        } else {
            addErro(atual(), "IDE");
            if (atual().getLexema().equals("[")) {
                Matriz();
            }
            if (seguinte().getTipo().equals("IDE")) {
                posicaoAtual = posicaoAtual + 1;
            } else {
                while (!(atual().getLexema().equals("true")
                        || atual().getLexema().equals("false")
                        || atual().getLexema().equals("(")
                        || atual().getTipo().equals("IDE")
                        || atual().getTipo().equals("string")
                        || atual().getTipo().equals("int")
                        || atual().getTipo().equals("float")
                        || atual().getLexema().equals("$"))) {
                    posicaoAtual = posicaoAtual + 1;
                }
                if (atual().getLexema().equals("$")) {
                    addErro(atual(), "fim de programa");
                }
            }
            if ((atual() != null) && (atual().getLexema().equals("="))) {
                posicaoAtual = posicaoAtual + 1;
                AtribuicaoValores();
                if ((atual() != null) && (atual().getLexema().equals(";"))) {
                    posicaoAtual = posicaoAtual + 1;
                    Comandos();
                } else {
                    addErro(atual(), "';'");
                    while (!(atual().getLexema().equals("write")
                            || atual().getLexema().equals("read")
                            || atual().getLexema().equals("for")
                            || atual().getLexema().equals("$")
                            || atual().getLexema().equals(".")
                            || atual().getTipo().equals("IDE")
                            || atual().getLexema().equals("if"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if ((atual().getLexema().equals("$"))) {
                        addErro(atual(), "fim de problema");
                    } else {
                        Comandos();
                    }
                }
            } else {
                addErro(atual(), "'='");
                if (seguinte().getLexema().equals("=")) {
                    posicaoAtual = posicaoAtual + 1;
                } else {
                    while (!(atual().getLexema().equals("true")
                            || atual().getLexema().equals("false")
                            || atual().getLexema().equals("(")
                            || atual().getTipo().equals("IDE")
                            || atual().getTipo().equals("string")
                            || atual().getTipo().equals("int")
                            || atual().getTipo().equals("float")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "fim de programa");
                    }
                    AtribuicaoValores();
                }
                if ((atual() != null) && (atual().getLexema().equals(";"))) {
                    posicaoAtual = posicaoAtual + 1;
                    Comandos();
                } else {
                    addErro(atual(), "';'");
                    while (!(atual().getLexema().equals("write")
                            || atual().getLexema().equals("read")
                            || atual().getLexema().equals("for")
                            || atual().getLexema().equals(".")
                            || atual().getLexema().equals("$")
                            || atual().getTipo().equals("IDE")
                            || atual().getLexema().equals("if"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if ((atual().getLexema().equals("$"))) {
                        addErro(atual(), "fim de problema");
                    } else {
                        Comandos();
                    }
                }
            }
        }
    }
    // <AtribuicaoValores> ::= Cadeia | 'true' | 'false' | <ExpAritmetica> |
    // Identificador '(' <ParametrosReais> ')'

    public void AtribuicaoValores() {
        if ((atual() != null) && (atual().getTipo().equals("IDE"))) {

            aux_tipo_2 = atual();

            Constantes c = new Constantes();
            c.setIde(aux_tipo_2);
            Variaveis v = new Variaveis();
            v.setIde(aux_tipo_2);

            if (tabela.buscaConstante(c) || tabela.buscaVariavel(v))
                tabela.verificaTipoAtribuicao(aux_tipo, aux_tipo_2);

            /*
             * aux_funcao = new Funcoes();
             * aux_funcao.setIde(aux_tipo_2);
             */

            posicaoAtual = posicaoAtual + 1;
            if (atual().getLexema().equals("(")) {

                /*
                 * if(!(tabela.buscaFuncao(aux_funcao)))
                 * tabela.addErro("A função '", aux_tipo_2.getLexema(),
                 * "' chamada na linha ", aux_tipo_2.getLinha(), " não foi declarada");
                 */
                tabela.addChamadaFun();
                tabela.setIdeChamadaFun(aux_tipo_2);

                posicaoAtual = posicaoAtual + 1;
                ParametrosReais();

                tabela.addListaChamadaFun();

                if (atual().getLexema().equals(")")) {
                    posicaoAtual = posicaoAtual + 1;
                } else {
                    addErro(atual(), "')'");
                    while (!(atual().getLexema().equals(")")
                            || atual().getLexema().equals(";")
                            || atual().getLexema().equals("}")
                            || atual().getLexema().equals(".")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "fim de programa");
                    }
                }
            } else if ((atual() != null) && ((atual().getLexema().equals("+")) || (atual().getLexema().equals("-")))) {
                posicaoAtual = posicaoAtual + 1;
                aux_tipo_2 = atual();
                tabela.verificaTipoOperacao(aux_tipo, aux_tipo_2);
                ExpAritmetica();
            } else if ((atual() != null) && ((atual().getLexema().equals("*")) || (atual().getLexema().equals("/")))) {
                posicaoAtual = posicaoAtual + 1;
                aux_tipo_2 = atual();
                tabela.verificaTipoOperacao(aux_tipo, aux_tipo_2);
                Termo();
                TermoAux();
            } else {
                tabela.VerificarIdeDeclarado(aux_tipo_2);
            }
        } else if ((atual() != null) && (atual().getTipo().equals("string")
                || atual().getLexema().equals("true") || atual().getLexema().equals("false"))) {

            aux_tipo_2 = atual();

            Constantes c = new Constantes();
            c.setIde(aux_tipo);
            c.setTipo(null);
            c.setValor(null);

            Variaveis v = new Variaveis();
            v.setIde(aux_tipo);
            v.setTipo(null);

            if (tabela.buscaConstante(c) || tabela.buscaVariavel(v))
                tabela.verificaTipoOperacaoTipos(aux_tipo, aux_tipo_2);

            posicaoAtual = posicaoAtual + 1;
        } else {
            ExpAritmetica();
        }
    }
    // <ParametrosReais> ::= <Valores> <MaisParametrosReais>

    public void ParametrosReais() {
        if (!(atual().getLexema().equals(")"))) {
            tabela.novoParChamada();
            tabela.addParChamada(atual());
        }
        valores();
        MaisParametrosReais();
    }
    // <MaisParametrosReais> ::= ',' <Valores> <MaisParametrosReais> | <>

    public void MaisParametrosReais() {
        if ((atual() != null) && (atual().getLexema().equals(","))) {
            posicaoAtual = posicaoAtual + 1;

            tabela.addParChamada(atual());

            valores();
            MaisParametrosReais();
        }
    }

    // <ExpLogica> ::= <OpLogicoNeg> '(' <ExpCondicional> ')' <ExpLogicaElse>
    public void ExpLogica() {
        OpLogicoNeg();
        if ((atual() != null) && (atual().getLexema().equals("("))) {
            posicaoAtual = posicaoAtual + 1;
            ExpCondicional();
            if ((atual() != null) && (atual().getLexema().equals(")"))) {
                posicaoAtual = posicaoAtual + 1;
                ExpLogicaElse();
            } else {
                addErro(atual(), "')'");
                ExpLogicaElse();
            }
        } else {
            addErro(atual(), "'('");
            ExpCondicional();
            if ((atual() != null) && (atual().getLexema().equals(")"))) {
                posicaoAtual = posicaoAtual + 1;
                ExpLogicaElse();
            } else {
                addErro(atual(), "')'");
                ExpLogicaElse();
            }
        }
    }

    // <ExpLogicaElse> ::= OpLogico <OpLogicoNeg> '(' <ExpCondicional> ')' | <>
    public void ExpLogicaElse() {
        if ((atual() != null) && ((atual().getLexema().equals("&&")) || (atual().getLexema().equals("||")))) {
            posicaoAtual = posicaoAtual + 1;
            OpLogicoNeg();
            if ((atual() != null) && (atual().getLexema().equals("("))) {
                posicaoAtual = posicaoAtual + 1;
                ExpCondicional();
                if ((atual() != null) && (atual().getLexema().equals(")"))) {
                    posicaoAtual = posicaoAtual + 1;
                } else {
                    addErro(atual(), "')'");
                    while (!(atual().getLexema().equals(")")
                            || atual().getLexema().equals(";")
                            || atual().getLexema().equals("}")
                            || atual().getLexema().equals(".")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "fim de programa");
                    }
                }
            } else {
                addErro(atual(), "'('");
                ExpCondicional();
                if ((atual() != null) && (atual().getLexema().equals(")"))) {
                    posicaoAtual = posicaoAtual + 1;
                } else {
                    addErro(atual(), "')'");
                    while (!(atual().getLexema().equals(")")
                            || atual().getLexema().equals(";")
                            || atual().getLexema().equals("}")
                            || atual().getLexema().equals(".")
                            || atual().getLexema().equals("$"))) {
                        posicaoAtual = posicaoAtual + 1;
                    }
                    if (atual().getLexema().equals("$")) {
                        addErro(atual(), "fim de programa");
                    }
                }
            }
        }
    }
    // <ExpCondicional> ::= <OpLogicoNeg> <ExpAritmetica> OpRelacional <OpLogicoNeg>
    // <ExpAritmetica>

    public void ExpCondicional() {
        OpLogicoNeg();
        aux_tipo = atual();
        ExpAritmetica();
        if ((atual() != null) && (opRel.contains(atual().getLexema()))) {
            posicaoAtual = posicaoAtual + 1;
            OpLogicoNeg();
            aux_tipo = atual();
            ExpAritmetica();
        } else {
            addErro(atual(), "ExpCondicional");
            while (!(atual().getLexema().equals(")")
                    || atual().getLexema().equals(";")
                    || atual().getLexema().equals("}")
                    || atual().getLexema().equals(".")
                    || atual().getLexema().equals("$"))) {
                posicaoAtual = posicaoAtual + 1;
            }
            if (atual().getLexema().equals("$")) {
                addErro(atual(), "fim de programa");
            }

        }
    }
    // <ExpAritmetica> ::= <Termo> Soma <ExpAritmetica> | <Termo>

    public void ExpAritmetica() {
        Termo();
        if ((atual() != null) && ((atual().getLexema().equals("+")) || (atual().getLexema().equals("-")))) {
            posicaoAtual = posicaoAtual + 1;
            ExpAritmetica();
        }
    }
    // <Termo> ::= <Fator> <TermoAux>

    public void Termo() {
        Fator();
        TermoAux();
    }
    // <TermoAux> ::= Mult <Termo> <TermoAux> | <>

    public void TermoAux() {

        if ((atual() != null) && ((atual().getLexema().equals("*")) || (atual().getLexema().equals("/")))) {
            posicaoAtual = posicaoAtual + 1;
            Termo();
            TermoAux();
        }
    }
    // <Fator> ::= '(' <ExpAritmetica> ')' | Identificador <Matriz>
    // <FatorIncremento> | int | float

    public void Fator() {
        if ((atual() != null) && (atual().getTipo().equals("IDE"))) {
            posicaoAtual = posicaoAtual + 1;
            if (atual().getLexema().equals("[")) {
                Matriz();
            }
            FatorIncremento();
        } else if ((atual().getTipo().equals("int") || atual().getTipo().equals("float"))) {

            aux_tipo_2 = atual();
            tabela.verificaTipoOperacaoTipos(aux_tipo, aux_tipo_2);

            posicaoAtual = posicaoAtual + 1;
        } else if ((atual() != null) && (atual().getLexema().equals("("))) {
            posicaoAtual = posicaoAtual + 1;
            aux_tipo = atual();
            ExpAritmetica();
            if ((atual() != null) && (atual().getLexema().equals(")"))) {
                posicaoAtual = posicaoAtual + 1;
            } else {
                addErro(atual(), "')'");
                while (!(atual().getLexema().equals("+")
                        || atual().getLexema().equals("-")
                        || atual().getLexema().equals("*")
                        || atual().getLexema().equals("/")
                        || atual().getLexema().equals(")")
                        || atual().getLexema().equals(";")
                        || atual().getLexema().equals(".")
                        || atual().getLexema().equals("}")
                        || atual().getLexema().equals("$"))) {
                    posicaoAtual = posicaoAtual + 1;
                }
                if (atual().getLexema().equals("$")) {
                    addErro(atual(), "fim de programa");
                }
            }
        } else {
            addErro(atual(), "'('");
            while (!(atual().getLexema().equals("+")
                    || atual().getLexema().equals("-")
                    || atual().getLexema().equals("*")
                    || atual().getLexema().equals("/")
                    || atual().getLexema().equals(")")
                    || atual().getLexema().equals(";")
                    || atual().getLexema().equals(".")
                    || atual().getLexema().equals("}")
                    || atual().getLexema().equals("$"))) {
                posicaoAtual = posicaoAtual + 1;
            }
            if (atual().getLexema().equals("$")) {
                addErro(atual(), "fim de programa");
            }

        }

    }
    // <FatorIncremento> ::= Incremento | <>

    public void FatorIncremento() {
        if ((atual() != null) && (atual().getLexema().equals("++"))) {
            posicaoAtual = posicaoAtual + 1;
        } else if ((atual() != null) && (atual().getLexema().equals("--"))) {
            posicaoAtual = posicaoAtual + 1;
        }
    }
    // <OpLogicoNeg> ::= '!' | <>

    public void OpLogicoNeg() {
        if ((atual() != null) && (atual().getLexema().equals("!"))) {
            posicaoAtual = posicaoAtual + 1;
        }
    }
}
