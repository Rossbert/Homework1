package service;

import authn.Secured;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.entities.User;

@Stateless
@Path("/customers")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class UserFacadeREST extends AbstractFacade<User> {
    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    public UserFacadeREST() {
        super(User.class);
    }

    // GET /rest/api/v1/customer
    @GET
    public List<User> findAllCustomers() {
        List<User> users = super.findAll();

        // Procesar usuarios para añadir enlaces HATEOAS
        users.forEach(user -> {
            if (!user.getArticles().isEmpty()) {
                // Añade enlace al último artículo
                user.setLink("/article/" + user.getArticles().get(0).getId());
            }
            user.setPassword(null); // Oculta la contraseña
        });

        return users;
    }

    // GET /rest/api/v1/customer/{id}
    @GET
    @Path("/{id}")
    public Response findCustomerById(@PathParam("id") Long id) {
        User user = em.find(User.class, id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        user.setPassword(null); // Oculta la contraseña
        if (!user.getArticles().isEmpty()) {
            user.setLink("/article/" + user.getArticles().get(0).getId());
        }

        return Response.ok(user).build();
    }

    // PUT /rest/api/v1/customer/{id}
    @PUT
    @Path("/{id}")
    @Secured
    public Response updateCustomer(@PathParam("id") Long id, User updatedUser) {
        User existingUser = em.find(User.class, id);
        if (existingUser == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Validar autorización
        if (!isAuthenticatedUser(existingUser.getUsername())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setArticles(updatedUser.getArticles());
        em.merge(existingUser);

        return Response.ok(existingUser).build();
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    private boolean isAuthenticatedUser(String username) {
        // Implementa la lógica para verificar si el usuario autenticado es válido
        return "authenticatedUser".equals(username); // Usuario de prueba
    }
}
