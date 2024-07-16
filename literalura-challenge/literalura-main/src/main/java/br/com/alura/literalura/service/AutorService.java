package br.com.alura.literalura.service;

import br.com.alura.literalura.repository.AutorRepository;
import br.com.alura.literalura.model.Autor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Scanner;

@Service
public class AutorService {
    @Autowired
    AutorRepository autorRepository;
    private Scanner leitura = new Scanner(System.in);

    private boolean isAnoValidado(String anoLido) {
        if (anoLido.length() == 4) {
            try {
                Integer.parseInt(anoLido);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public void listarAutoresRegistrados() {
        List<Autor> autores = autorRepository.findAll();
        if (autores.isEmpty()) {
            System.out.println("Nenhum autor localizado!");
            return;
        }
        autores.forEach(System.out::println);
    }

    public void listarAutorVivoEmDeterminadoAno() {

        System.out.println("Digite o ano desejado:");
        String anoLido = leitura.nextLine();

        if (isAnoValidado(anoLido)) {
            List<Autor> autoresFiltados = autorRepository.listaAutoresVivosEmDeterminadoAno(Integer.parseInt(anoLido));
            if (autoresFiltados.isEmpty()) {
                System.out.println("Nnenhum autor com o perfil desejado!");
                return;
            }
            autoresFiltados.forEach(System.out::println);
        } else {
            System.out.println("Ano inválido, digite no formato a seguir: XXXX");
        }
    }

    public void listarAutorPorNome() {
        System.out.println("Nome do autor a ser localizado?");
        String nome = leitura.nextLine();
        List<Autor> autores = autorRepository.listarAutorPorNome(nome);
        if (!autores.isEmpty()) {
            autores.stream()
                    .distinct()
                    .forEach(System.out::println);
        } else {
            System.out.println("Nenhum autor localizado no Banco de Dados.");
        }
    }
}
