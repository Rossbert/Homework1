package service;

import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.entities.Article;

@Stateless
@Path("/articles")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ArticleFacadeREST extends AbstractFacade<Article> {

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    public ArticleFacadeREST() {
        super(Article.class);
    }

    // GET /rest/api/v1/article?topic=${topic}&author=${author}
    @GET
    public List<Article> findArticles(
            @QueryParam("topic") String topic,
            @QueryParam("author") String author) {
        String queryStr = "SELECT a FROM Article a WHERE 1=1";
        if (topic != null && !topic.isEmpty()) {
            queryStr += " AND a.topics LIKE :topic";
        }
        if (author != null && !author.isEmpty()) {
            queryStr += " AND a.author.name = :author";
        }
        queryStr += " ORDER BY a.popularity DESC"; // Popularidad
        TypedQuery<Article> query = em.createQuery(queryStr, Article.class);
        if (topic != null && !topic.isEmpty()) {
            query.setParameter("topic", "%" + topic + "%");
        }
        if (author != null && !author.isEmpty()) {
            query.setParameter("author", author);
        }
        return query.getResultList();
    }

    // GET /rest/api/v1/article/{id}
    @GET
    @Path("/{id}")
    public Response findArticleById(@PathParam("id") Long id) {
        Article article = em.find(Article.class, id);
        if (article == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Incrementa el contador de visualizaciones
        article.setPopularity(article.getPopularity() + 1);
        em.merge(article);

        return Response.ok(article).build();
    }

    // DELETE /rest/api/v1/article/{id}
    @DELETE
    @Path("/{id}")
    public Response deleteArticle(@PathParam("id") Long id) {
        Article article = em.find(Article.class, id);
        if (article == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Lógica para verificar si el usuario autenticado es el autor
        if (!isAuthorAuthenticated(article.getAuthor())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        em.remove(article);
        return Response.noContent().build();
    }

    // POST /rest/api/v1/article
    @POST
    public Response createArticle(Article entity) {
        if (entity.getTopics() == null || entity.getAuthor() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        super.create(entity);
        return Response.status(Response.Status.CREATED)
                .entity(entity)
                .build();
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    private boolean isAuthorAuthenticated(Object author) {
        // Implementar lógica para verificar si el usuario autenticado es el autor
        return true;
    }
}
