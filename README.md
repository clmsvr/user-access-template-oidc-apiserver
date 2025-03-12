### Extenção do projeto do Repositório [user-access-template-oidc](https://github.com/clmsvr/user-access-template-oidc) incluindo uma API Rest (Resouce Server) para acesso e gerenciamento dos Usuários.

Recursos Adicionados

* [Spring Oauth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)

* [Swagger UI](https://swagger.io/tools/swagger-ui/) com [SpringDoc](https://springdoc.org/)



### Funções adicionadas

* API Rest para acesso e atualização de dados do usuário.
* API Doc com Swagger UI e SpringDoc.

#### Setup

Em adição à configuração do projeto [user-access-template-oidc](https://github.com/clmsvr/user-access-template-oidc), precisamos:

* Na sessão **Login Pages** do **App Client**  adicionar as **Allowed callback URLs** :
    * http://localhost:8080/authorize/oauth2/code/cognito   (p/ OAuth2 Client - uso futuro)
    * http://localhost:8080/swagger-ui/oauth2-redirect.html (p/ uso do Swagger - opcional)

* Configurar a propriedade **spring.security.oauth2.resourceserver.jwt.jwk-set-uri** com o valor encontrado em **Token signing key URL** na página do pool.
    Ex: spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_FSSjl3xir/.well-known/jwks.json
    

