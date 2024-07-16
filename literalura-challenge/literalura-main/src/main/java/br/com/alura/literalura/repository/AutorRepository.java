package br.com.alura.literalura.repository;

import br.com.alura.literalura.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {

    @Query("SELECT a FROM Autor a WHERE :ano > a.anoNascimento AND :ano < a.anoFalecimento")
    List<Autor> listaAutoresVivosEmDeterminadoAno(Integer ano);

    @Query("SELECT a FROM Autor a WHERE a.nome ILIKE %:nome%")
    List<Autor> listarAutorPorNome(String nome);
}
