# CRUD Acadêmico — Spring Boot + Angular

Sistema completo de CRUD implementando o domínio do diagrama:
`Person` (abstrata) → `Student`, `Professor` + `Address` (1-N) + `Status` (enum `ACTIVE`/`DISABLE`).

## Arquitetura

```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   Frontend   │──┬──▶│   Backend    │─────▶│  PostgreSQL  │
│   Angular 18 │  │   │ Spring Boot  │      │      16      │
│   + Material │  │   │   Java 21    │      └──────────────┘
│   (nginx)    │  │   └──────────────┘
└──────────────┘  │
   :4200          │ /api → :8080
                  │ /uploads → :8080
```

## Stack

**Backend** (`/backend`)
- Java 21 (LTS)
- Spring Boot 3.3.5 (Web, Data JPA, Validation)
- H2 em memória (testes) + PostgreSQL 16 (Docker)
- springdoc-openapi (Swagger UI)
- JUnit 5 + Mockito + AssertJ + MockMvc
- Maven

**Frontend** (`/frontend`)
- Angular 18 (standalone components + signals)
- Angular Material (componentes + tema azure-blue)
- Reactive Forms + RxJS
- Máscaras de input: telefone `(00) 00000-0000`, CEP `00000-000`, salário BRL
- nginx (serve estático + proxy reverso `/api` e `/uploads`)

**Infra**
- Docker / Docker Compose

## Pré-requisitos

- **Docker** + **Docker Compose** (única dependência — não precisa de Java, Maven nem Node instalados)

## Subir tudo (banco + back + front)

```bash
docker compose up --build
```

Acessos:

| Serviço     | URL                                       |
|-------------|-------------------------------------------|
| Frontend    | <http://localhost:4200>                   |
| Backend API | <http://localhost:8080/api/v1/students>   |
| Swagger UI  | <http://localhost:8080/swagger-ui.html>   |
| Postgres    | `localhost:5432` (user/pass `crud`/`crud`)|

Parar e limpar volumes:

```bash
docker compose down -v
```

### Subir só o backend (para rodar o front em modo dev)

```bash
docker compose up --build db backend
```

Depois, no diretório `frontend/`, com Node 20 instalado:

```bash
npm install
npm start   # ng serve em http://localhost:4200 com proxy.conf.json
```

## Rodar os testes do backend via Docker

```bash
docker run --rm -v "${PWD}/backend:/work" -w /work maven:3.9-eclipse-temurin-21 mvn test
```

No CMD use `%cd%\backend` em vez de `${PWD}/backend`.

Cobertura:
- **Unitários** (`*Test`) — services com Mockito
- **Integração** (`*IT`) — `@SpringBootTest` + MockMvc, fluxo CRUD ponta a ponta

## Endpoints principais

Base: `/api/v1`

| Método | Path                                            | Descrição                        |
|--------|-------------------------------------------------|----------------------------------|
| POST   | `/students`                                     | Cria student                     |
| GET    | `/students?page=0&size=10&includeDisabled=false`| Lista paginada                   |
| GET    | `/students/{id}`                                | Busca por id                     |
| PUT    | `/students/{id}`                                | Atualiza                         |
| DELETE | `/students/{id}`                                | Soft delete (status=DISABLE)     |
| POST   | `/students/{id}/restore`                        | Restaura para ACTIVE             |
| POST   | `/students/{id}/photo` *(multipart)*            | Upload da foto (campo `file`)    |
| DELETE | `/students/{id}/photo`                          | Remove a foto do estudante       |
| ...    | `/professors/*`                                 | Mesma estrutura (sem `/photo`)   |
| GET    | `/persons/{personId}/addresses`                 | Lista endereços                  |
| POST   | `/persons/{personId}/addresses`                 | Adiciona endereço                |
| PUT    | `/persons/{personId}/addresses/{addressId}`     | Atualiza endereço                |
| DELETE | `/persons/{personId}/addresses/{addressId}`     | Remove endereço                  |

### Exemplo: criar um Student

```bash
curl -X POST http://localhost:8080/api/v1/students \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Maria Silva",
    "phoneNumber": "(11) 99999-8888",
    "emailAddress": "maria@example.com",
    "studentNumber": "STU-2026-001",
    "addresses": [
      {
        "street": "Rua A, 100",
        "city": "São Paulo",
        "state": "SP",
        "zipCode": "01234-567",
        "country": "Brasil"
      }
    ]
  }'
```

### Exemplo: upload da foto

```bash
curl -X POST http://localhost:8080/api/v1/students/1/photo \
  -F "file=@/caminho/para/foto.jpg"
```

A resposta retorna o `Student` atualizado com `photo` apontando para `/uploads/{uuid}.{ext}`.
A imagem fica acessível em `http://localhost:8080/uploads/{uuid}.{ext}` (ou via nginx em `:4200`).

Tipos aceitos: **JPEG, PNG, WEBP, GIF**. Limite: **5 MB**. Ao substituir uma foto, o arquivo antigo é apagado do disco.

## Estrutura do projeto

```
.
├── backend/
│   ├── src/main/java/br/com/academico/crud/
│   │   ├── CrudApplication.java
│   │   ├── config/         OpenApiConfig, CorsConfig, StaticResourceConfig, UploadProperties
│   │   ├── controller/     StudentController, ProfessorController, AddressController
│   │   ├── service/        *Service (regras de negócio), PhotoStorageService
│   │   ├── repository/     JpaRepository de cada entidade
│   │   ├── domain/
│   │   │   ├── entity/     Person (abstract), Student, Professor, Address
│   │   │   └── enums/      Status
│   │   ├── dto/            *Request / *Response (records + Bean Validation)
│   │   └── exception/      GlobalExceptionHandler + custom exceptions
│   ├── src/test/java/...   Unit + integration tests
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/
│   ├── src/app/
│   │   ├── core/           models.ts, *.service.ts, error.interceptor.ts
│   │   ├── shared/         address-form (+ máscara CEP), confirm-dialog
│   │   ├── features/
│   │   │   ├── students/   student-list, student-form (+ upload foto + máscara tel)
│   │   │   └── professors/ professor-list, professor-form (+ máscara tel/salário BRL)
│   │   ├── app.component.ts (shell + toolbar)
│   │   ├── app.config.ts   (providers: router, http, animations, material, LOCALE_ID pt-BR)
│   │   └── app.routes.ts   (lazy routes)
│   ├── nginx.conf          (SPA fallback + /api e /uploads proxy)
│   ├── proxy.conf.json     (dev only — ng serve → backend:8080)
│   ├── package.json
│   └── Dockerfile
│
├── .gitignore
├── docker-compose.yml      orquestra db + backend + frontend
└── README.md
```

## Decisões técnicas

### Backend

- **Herança JPA**: `@Inheritance(JOINED)` — tabelas `person`, `student`, `professor` separadas com FK. Casa com o UML e mantém o modelo normalizado.
- **Soft delete**: `DELETE` apenas troca `status` para `DISABLE`. Endpoint `POST /{id}/restore` reverte. Usa o enum `Status` de forma significativa.
- **DTOs como records** com factory methods (`from`, `toEntity`, `applyTo`) em vez de MapStruct — menos boilerplate, mapeamento explícito.
- **Erros padronizados em RFC 7807** (`ProblemDetail`) via `@RestControllerAdvice` global.
- **Bean Validation** em todos os DTOs de entrada.
- **H2 para testes, Postgres no Docker** via profiles — sem fricção local, pronto para banco real.

### Upload de fotos

- **Storage em disco** dentro do container (`/data/uploads`) montado em volume Docker `crud-uploads` — persiste entre rebuilds.
- **Nomes únicos** com `UUID + extensão` — evita colisão e *path traversal*.
- **Validação de content-type** (`image/jpeg|png|webp|gif`) + cap de **5 MB** via `spring.servlet.multipart.max-file-size`.
- **Arquivos servidos como recurso estático** em `/uploads/**` pelo Spring (`StaticResourceConfig`), com cache de 1h.
- **Substituição segura**: ao trocar a foto, o arquivo antigo é deletado depois do `save()` — se a transação falha, mantém o antigo.
- **nginx** faz proxy de `/uploads/*` pro backend em produção; em dev (`ng serve`) o `proxy.conf.json` espelha o comportamento.

### Frontend

- **Standalone components** (sem `NgModule`) + **signals** para estado reativo — padrão moderno do Angular 18.
- **Lazy routing** — cada feature carregada sob demanda.
- **Reactive Forms** com validação espelhando o backend (required, email, min/max length).
- **Máscaras de input** implementadas sem biblioteca externa:
  - Telefone: `(DD) XXXX-XXXX` (fixo) / `(DD) XXXXX-XXXX` (celular), dinâmico conforme o número de dígitos.
  - CEP: `XXXXX-XXX`.
  - Salário: formatação BRL em tempo real com `Intl.NumberFormat`, prefixo `R$` via `matTextPrefix`.
- **Locale pt-BR** registrado globalmente via `LOCALE_ID` — `currency` pipe formata automaticamente em Real.
- **HTTP interceptor** centraliza tratamento de erros e exibe snackbar com o `detail` do `ProblemDetail`.
- **nginx + proxy** — em produção `/api/*` e `/uploads/*` são proxied para o serviço `backend`, eliminando CORS.