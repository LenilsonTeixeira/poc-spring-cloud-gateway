# POC - Spring cloud gateway com autenticação no Keycloak



O serviço "api-partner" possui dois recursos cadastrados: "/parceiros/magalu" e "/parceiros/gpa", como premissa, cada recurso deve ser acessado por meio de **aplicações distintas** autenticadas no Keycloak.  A camada de autenticação e validação das permissões de acesso de uma determinada aplicação é realizado no api gateway através de autenticação  no Keycloak e validação de acesso ao recurso com base em informação extraída na decodificação do access token.

Payload de um token decodificado:

```
{
  "exp": 1601223996,
  "iat": 1601223696,
  "jti": "a33ea8e0-b018-4662-a6b4-831d3c823684",
  "iss": "http://localhost:8080/auth/realms/app-backend",
  "aud": "gpa",
  "sub": "556c88ae-9e1b-4f2a-8b83-7bbd0593258d",
  "typ": "Bearer",
  "azp": "gpa",
  "session_state": "16227088-e732-4b20-ae4f-7a8bf072e8e8",
  "acr": "1",
  "allowed-origins": [
    "http://localhost:8081"
  ],
  "scope": "profile email",
  "clientHost": "172.17.0.1",
  "clientId": "gpa",
  "email_verified": false,
  "preferred_username": "service-account-gpa",
  "clientAddress": "172.17.0.1"
}
```

**Importante:** A informação extraída do access token é a do atributo **apz**  que é utilizada na regra de negócio criado no gateway para validar o acesso a um determinado recurso.



Executar keycloak (Deve ser executado na pasta raiz "poc-spring-cloud-gateway"):

```
docker run  -p 8080:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin \
    -e KEYCLOAK_IMPORT=/tmp/realm-export.json -v $(pwd)/keycloak/realm-export.json:/tmp/realm-export.json jboss/keycloak
```



Configurações necessárias:

1 - Acessar o administration console: http://localhost:8080/auth/

2 - **Usuário**: admin | **Senha**: admin

3- Regerar secrets para as apps **magalu** e **gpa** 

​       -- Menu lateral **Clients** > magalu > Credentials > Regenerate Secret

​       -- Menu lateral **Clients** > gpa > Credentials > Regenerate Secret

Após a realização das configurações necessárias, é possivel testar a atenticação:

OBS: Adicionar valor da secret no curl abaixo.

App Magalu:

```
curl -X POST 'http://localhost:8080/auth/realms/app-backend/protocol/openid-connect/token' \
 --header 'Content-Type: application/x-www-form-urlencoded' \
 --data-urlencode 'grant_type=client_credentials' \
 --data-urlencode 'client_id=magalu' \
 --data-urlencode 'client_secret=SECRET GERADA' 
```

 App GPA: 

```
curl -X POST 'http://localhost:8080/auth/realms/app-backend/protocol/openid-connect/token' \
 --header 'Content-Type: application/x-www-form-urlencoded' \
 --data-urlencode 'grant_type=client_credentials' \
 --data-urlencode 'client_id=magalu' \
 --data-urlencode 'client_secret=SECRET GERADA' 
```



Por fim o curl para teste no api gateway:

Cenário 1:

App GPA tentando acessar um recurso permitido somente a app magalu - Resultado esperado Http Status 403

```
curl --location --request GET 'http://localhost:8081/parceiros/magalu' \
--header 'client_id: gpa' \
--header 'client_secret: SECRET GERADA'
```



Cenário 2:

App GPA tentando acessar um recurso permitido somente a app gpa - Resultado esperado Http Status 200

```
curl --location --request GET 'http://localhost:8081/parceiros/gpa' \
--header 'client_id: gpa' \
--header 'client_secret: SECRET GERADA'
```



Cenário 3: 

App GPA falha na autenticação com credenciais inválidas - Resultado esperado Http Status 401

```
curl --location --request GET 'http://localhost:8081/parceiros/gpa' \
--header 'client_id: gpa' \
--header 'client_secret: ed28c264-cd7d-459f-935b-99082d5600e4'
```

