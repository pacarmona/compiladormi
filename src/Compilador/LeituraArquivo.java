
package Compilador;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Patricia Carmona
 */
public class LeituraArquivo {

    private String localFile;
    private String[] nomeArquivo;

    public ArrayList<String> leitura() {

        ArrayList<String> code = new ArrayList<>();
        File access = new File("test/Entrada/");
        for (File aux : access.listFiles()) {
            code.add(aux.getName());
        }

        return code;
    }

    public ArrayList<String> lerArquivo(String localFile) throws FileNotFoundException {

        ArrayList<String> code;
        try (Scanner scanner = new Scanner(new FileReader("test/Entrada/" + localFile))) {
            this.localFile = localFile;
            nomeArquivo = this.localFile.split(".txt");
            code = new ArrayList<>();

            while (scanner.hasNextLine()) {
                String aux = scanner.nextLine();
                code.add(aux);
            }
        }
        return code;

    }

    public void escreverArquivo(ArrayList<Token> tokens, ArrayList<String> erros) throws IOException {

        try (FileWriter file = new FileWriter("test/Saida/" + this.nomeArquivo[0] + "-lex.txt", false)) {
            PrintWriter gravar = new PrintWriter(file);

            tokens.forEach((token) -> {
                gravar.println(token.getLinha() + " " + token.getTipo() + " " + token.getLexema());
            });

            if (erros.isEmpty())
                gravar.println("\n Nao existem erros lexicos");
            else {
                gravar.println("");
                erros.forEach((erro) -> {
                    gravar.println(erro);
                });
            }
        }

    }

    void escreverArquivoSintatico(ArrayList<String> listarErros) throws IOException {
        try (FileWriter file = new FileWriter("test/Saida/" + this.nomeArquivo[0] + "-sint.txt", false)) {
            PrintWriter gravar = new PrintWriter(file);

            if (listarErros.isEmpty())
                gravar.println("\n Nao existem erros Sintaticos");
            else {

                for (int i = 0; i < listarErros.size(); i++) {
                    gravar.println(listarErros.get(i));
                }

            }
        }
    }

    void escreverArquivoSemantico(ArrayList<String> listarErros) throws IOException {
        try (FileWriter file = new FileWriter("test/Saida/" + this.nomeArquivo[0] + "-sem.txt", false)) {
            PrintWriter gravar = new PrintWriter(file);

            if (listarErros.isEmpty())
                gravar.println("\n Nao existem erros Semanticos");
            else {

                for (int i = 0; i < listarErros.size(); i++) {
                    gravar.println(listarErros.get(i));
                }

            }
        }
    }

    public String getLocalFile() {
        return localFile;
    }

}
