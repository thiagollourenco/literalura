package br.com.alura.literalura.service;

import br.com.alura.literalura.model.Autor;
import br.com.alura.literalura.model.DadosApi;
import br.com.alura.literalura.model.DadosLivro;
import br.com.alura.literalura.model.Livro;
import br.com.alura.literalura.repository.LivroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LivroService {
    @Autowired
    LivroRepository livroRepository;
    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private Scanner leitura = new Scanner(System.in);

    public void buscarLivro() {
        System.out.println("Digite o nome do livro que você deseja encontar e salvar.");
        String nome = leitura.nextLine();
        System.out.println("Pesquisando...");
        String json = consumoApi.obterDados(URL_BASE + "?search=" + nome.replace(" ", "%20"));
        DadosApi dadosApi = conversor.obterDados(json, DadosApi.class);

        if (dadosApi.quantidade() == 0) {
            System.out.println("Nenhum livro encontrado.");
        } else if (dadosApi.quantidade() == 1) {
            obterLivro(dadosApi);
        } else {
            System.out.println("Foram encontrados " + dadosApi.quantidade() + " livros. Deseja visualizar todos e salvar? (S/N)");
            String opcao = leitura.nextLine();
            if (opcao.equalsIgnoreCase("s")) {
                obterTodosLivros(dadosApi);
            }
        }
    }

    private void obterLivro(DadosApi dadosApi) {

        List<Autor> autores = dadosApi.livros().stream().flatMap(l -> l.autores().stream()).map(Autor::new).toList();

        dadosApi.livros().stream()
                .distinct()
                .forEach(l -> {
                    List<Autor> livroAutores = l.autores().stream()
                            .map(a -> autores.stream()
                                    .filter(existingAutor -> existingAutor.getNome().equals(a.nome()))
                                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Autor não encontrado: ")))
                            .collect(Collectors.toList());
                    Livro livro = new Livro(new DadosLivro(l.titulo(), l.idioma(), l.autores(), l.numeroDownloads()));
                    livro.setAutores(livroAutores);
                    try {
                        livroRepository.save(livro);
                        System.out.println(livro);
                    } catch (DataIntegrityViolationException e) {
                        System.out.println(livro);
                        System.err.println("Erro ao salvar o livro "+livro.getTitulo()+", este livro já foi salvo.");
                    } catch (InvalidDataAccessApiUsageException e) {
                        System.err.println("Erro.");
                    }
                });

    }

    private void obterTodosLivros(DadosApi dadosApi) {
        boolean proximo = true;
        String json;

        for (int i = 1; i <= dadosApi.quantidade(); i++) {
            if (proximo) {
                proximo = false;
                obterLivro(dadosApi);
            } else if (dadosApi.proximo() == null) {
                break;
            } else {
                System.out.println("Buscando...");
                json = consumoApi.obterDados(dadosApi.proximo());
                dadosApi = conversor.obterDados(json, DadosApi.class);
                obterLivro(dadosApi);
            }
        }
    }

    public void listarLivrosRegistrados() {
        System.out.println("Buscando...");
        List<Livro> livros = livroRepository.findAll();
        if (livros.isEmpty()) {
            System.out.println("Não há livros cadastrados");
            return;
        }
        if (livros.size() <= 3) {
            livros.forEach(System.out::println);
        } else {
            System.out.println("Foram encontrados " + livros.size() + " livros. Deseja visualizar todos e salvar? (S/N)");
            String opcao = leitura.nextLine();
            if (opcao.equalsIgnoreCase("s")) {
                livros.forEach(System.out::println);
            }
        }
    }

    public void listarLivrosEmDeterminadoIdioma() {

        String menuIdioma = """
                                            
                Digite o idioma para realizar desejável:
                Espanhol
                Inglês
                Francês
                Português
                """;
        System.out.println(menuIdioma);
        String idioma = leitura.nextLine();

        if (!idioma.equalsIgnoreCase("es") && !idioma.equalsIgnoreCase("en") && !idioma.equalsIgnoreCase("fr") && !idioma.equalsIgnoreCase("pt")) {

            System.out.println("Idioma não encontrado. Escolha entre os a seguir: Espanhol, Inglês, Francês ou Português");
            return;
        }
        List<Livro> livrosFiltrados = livroRepository.livrosEmDeterminadoIdioma(idioma);

        if (livrosFiltrados.isEmpty()) {
            System.out.println("Não existe livros neste idioma.");
            return;
        }
        livrosFiltrados.forEach(System.out::println);
    }

    public void listarTop10LivrosMaisBaixados() {

        System.out.println("10 livros mais baixados da API ou dos registros? (A/R)");
        String opcao = leitura.nextLine();

        if (opcao.equalsIgnoreCase("a")) {
            listarTop10Api();
        } else if (opcao.equalsIgnoreCase("r")) {
            listarTop10BancoDeDados();
        } else {
            System.out.println("Opção inválida! Escolha entre (A), (R)");
        }
    }

    private void listarTop10Api() {
        System.out.println("Buscando...");
        String json = consumoApi.obterDados(URL_BASE + "?sort=popular");
        DadosApi dadosApi = conversor.obterDados(json, DadosApi.class);

        if (dadosApi.quantidade() == 0) {
            System.out.println("Não foram encontrados livros nessa pesquisa");
        } else {
            obter10LivrosMaisPopulares(dadosApi);
        }
    }

    private void obter10LivrosMaisPopulares(DadosApi dadosApi) {
        List<Livro> livrosFiltrados = dadosApi.livros().stream().limit(10).map(l -> new Livro(new DadosLivro(l.titulo(), l.idioma(), l.autores(), l.numeroDownloads()))).toList();
        livrosFiltrados.forEach(System.out::println);
    }

    private void listarTop10BancoDeDados() {
        List<Livro> livros = livroRepository.top10LivrosMaisBaixados();
        if (livros.isEmpty()) {
            System.out.println("Não foi encontrado nenhum livro no banco de dados");
            return;
        }
        livros.forEach(System.out::println);
    }
}