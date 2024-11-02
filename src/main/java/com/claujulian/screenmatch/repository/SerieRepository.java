package com.claujulian.screenmatch.repository;

import com.claujulian.screenmatch.model.Categoria;
import com.claujulian.screenmatch.model.Episodio;
import com.claujulian.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {
    Optional<Serie> findByTituloContainsIgnoreCase(String nombreSerie);

    List<Serie> findTop5ByOrderByEvaluacionDesc();

    List<Serie> findByGenero(Categoria categoria);

    //List<Serie> findByTotalDeTemporadasLessThanEqualAndEvaluacionGreaterThanEqual(int totalTemporadas, double evaluacion);

    //Se puede trabajar con lenguaje nativo o JPQL que es como Java trabaja el lenguaje nativo de las bases de dato

    @Query("SELECT s FROM Serie s WHERE s.totalDeTemporadas >= :totalDeTemporadas AND s.evaluacion >= :evaluacion")
    List<Serie> seriesPorTemporadasYEvaluacion(int totalDeTemporadas, double evaluacion);

    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE e.titulo ILIKE %:nombreEpisodio%")
    List<Episodio> episodiosPorNombre(String nombreEpisodio);

    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s = :serie ORDER BY e.evaluacion DESC LIMIT 5")
    List<Episodio> episodios5Mejores(Serie serie);
}
