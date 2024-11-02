package com.claujulian.screenmatch.principal;

import com.claujulian.screenmatch.model.*;
import com.claujulian.screenmatch.repository.SerieRepository;
import com.claujulian.screenmatch.service.ConsumoAPI;
import com.claujulian.screenmatch.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;


public class Principal {
    private Scanner teclado = new Scanner(System.in);

    private final String URL = "https://www.omdbapi.com/?t=";
    private final String APIKEY = "&apikey=205d9a2a";

    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();

    private List<DatosSerie> datosSeries = new ArrayList<>();
    private SerieRepository repositorio;

    private List<Serie> series;
    Optional<Serie> serieBuscada;

    public Principal(SerieRepository repository) {
        this.repositorio=repository;
    }

    // Metodo para mostrar resultados en la clase Principal.java

    public void muestraElMenu(){

        var opcion = -1;

        while(opcion != 0){

            var menu = """
                    1 - Buscar Serie
                    2 - Buscar Episodio
                    3 - Mostrar Serie Buscada
                    4 - Buscar Series por Titulo
                    5 - Top 5 mejores series
                    6 - Buscar Series por Categoria
                    7 - Filtrar Series
                    8 - Buscar Episodio por Titulo
                    9 - Top 5 mejores episodios de tu serie
                    
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine(); // para que no hay problemas de lectura

            switch (opcion){
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriesPorTitulo();
                    break;
                case 5:
                    buscarTop5Series();
                    break;
                case 6:
                    buscarSeriesPorCategoria();
                    break;
                case 7:
                    filtrarSeries();
                    break;
                case 8:
                    buscarEpisodioPorTitulo();
                    break;
                case 9:
                    buscarTop5Episodios();
                    break;
                case 0:
                    System.out.println("Saliendo del Aplicativo");
                    break;
                default:
                    System.out.println("Opcion no válida");
            }
        }
    }

        private DatosSerie getDatosSerie() {
            System.out.println("\n Escribe el nombre de la serie que deseas buscar:");
            var nombreSerie = teclado.nextLine();

            var json = consumoAPI.obtenerDatos(URL + nombreSerie.replace(" ", "+") + APIKEY);
            DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
            return datos;
        }

        // Crea una lista de temporadas y nombre de episodios de la serie seleccionada

        private void buscarEpisodioPorSerie() {

            //DatosSerie datosSerie = getDatosSerie();
            mostrarSeriesBuscadas();
            System.out.println("Elige la serie que quieres ver sus episodios: ");
            var nombreSerie=teclado.nextLine();
            Optional<Serie> serie = series.stream()
                    .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                    .findFirst();

            if(serie.isPresent()){
                var serieEncontrada = serie.get();
                List<DatosTemporada> temporadas = new ArrayList<>();

                for (int i = 1; i <= serieEncontrada.getTotalDeTemporadas(); i++) {
                    var json = consumoAPI.obtenerDatos(URL + serieEncontrada.getTitulo().replace(" ", "+") + "&Season=" + i + APIKEY);
                    DatosTemporada datosTemporada = conversor.obtenerDatos(json, DatosTemporada.class);
                    temporadas.add(datosTemporada);
                }
                temporadas.forEach(System.out::println);

                List<Episodio> episodios = temporadas.stream()
                        .flatMap(d -> d.episodios().stream()
                                .map(e -> new Episodio(d.numero(), e)))
                        .collect(Collectors.toList());

                serieEncontrada.setEpisodios(episodios);
                repositorio.save(serieEncontrada);


            }

        }

        private void buscarSerieWeb(){
            DatosSerie datos = getDatosSerie();


            Serie serie = new Serie(datos);
            repositorio.save(serie);
            // datosSeries.add(datos);
            System.out.println(datos);
        }

        private void mostrarSeriesBuscadas(){
              series = repositorio.findAll();
//            List<Serie> series = new ArrayList<>();
//            series = datosSeries.stream()
//                    .map(d -> new Serie(d))
//                    .collect(Collectors.toList());

            series.stream()
                    .sorted(Comparator.comparing(Serie::getGenero))
                    .forEach(System.out::println);

        }

        private void buscarSeriesPorTitulo(){
            System.out.println("Elige la serie que quieres buscar: ");
            var nombreSerie=teclado.nextLine();

            serieBuscada = repositorio.findByTituloContainsIgnoreCase(nombreSerie);

            if(serieBuscada.isPresent()){
                System.out.println("La serie buscada es " + serieBuscada.get());
            }else{
                System.out.println("Serie no encontrada.");
            }
        }

        private void buscarTop5Series(){

        List<Serie> topSeries = repositorio.findTop5ByOrderByEvaluacionDesc();

        topSeries.forEach(s -> System.out.println("Serie " + s.getTitulo() + " Evaluacion: " + s.getEvaluacion()) );
    }

    private void buscarSeriesPorCategoria(){
        System.out.println("Ingrese la categoria que desea buscar: ");
        var genero = teclado.nextLine();
        var categoria = Categoria.fromEspanol(genero);

        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Las series del genero " + genero + " son: ");
        seriesPorCategoria.forEach(System.out::println);

    }

    private void filtrarSeries(){
        System.out.println("Ingrese el total de temporadas limite: ");
        var totalDeTemporadas = teclado.nextInt();
        teclado.nextLine();
        System.out.println("Ingrese la evaluación requerida: ");
        var evaluacion = teclado.nextDouble();
        teclado.nextLine();

        List<Serie> seriesFiltradas = repositorio.seriesPorTemporadasYEvaluacion(totalDeTemporadas, evaluacion);
        System.out.println("Las series filtradas son: ");
        seriesFiltradas.forEach(s->
                System.out.println(s.getTitulo() + " Evaluacion: " + s.getEvaluacion()));

    }

    private void buscarEpisodioPorTitulo(){
        System.out.println("Ingrese el Titulo del Episodio que desea buscar : ");
        var nombreEpisodio = teclado.nextLine();
        //teclado.nextLine();

        List<Episodio> episodiosEncontrados = repositorio.episodiosPorNombre(nombreEpisodio);

        episodiosEncontrados.forEach(e ->
                System.out.printf("Serie: %s Temporada %s Episodio %s Evaluación %s\n",
                e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getEvaluacion()
                ));
    }

    private void buscarTop5Episodios(){
        buscarSeriesPorTitulo();

        if(serieBuscada.isPresent()){
            Serie serie = serieBuscada.get();

            List<Episodio> episodiosMejores = repositorio.episodios5Mejores(serie);
            episodiosMejores.forEach(e ->
                    System.out.printf("Serie: %s Temporada %s Episodio %s Evaluación %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(), e.getTitulo(), e.getEvaluacion()
                    ));
        }


    }

}



