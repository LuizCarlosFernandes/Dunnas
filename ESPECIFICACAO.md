### 1 Imprecisões e comportamentos inesperados encontrados no código.


#### 1.1. Pacotes Java declarados divergentes da estrutura de pastas em todos os testes.
  - O que foi encontrado: Todos o arquivos estão fisicamentes organizandos em src/test/.../unit/... e src/test/...integration/..., mas o package declada no topo de 15 arquivos ainda apontava para o pacote da classe de produção corresponte.
  - Investigação feita antes de decidir: verificado se alguma classe de produção testada expõe os métodos, campos ou contrutores package-private dos quais os testes dependessem, como não foi encontrando nenhum caso onde o acesso é feito por membros public. 
  - Decisão: Corrigido o package declarado de cada um dos 15 arquivos para bater com sua pasta física, e adicionando o import explícito da classe de produção que antes era resolvido implicitamente por estar no mesmo pacote. Nenhum teste teve sou lógica ou asserções alteradas.

#### 1.2. @WebMvcTest sem mock de JwtService quebrava toda a suíte de testes web.
  - O que foi encontrado: ao rodar ./mvnw teste na base recebida, 22 de 56 testes falhavam antes mesmo de qualquer asserção.
  - Decisão: corrigido adicionando @MockitoBean private JwtService jwtService; 
  - Porque corrigir: um baseline de testes quebrados antes de qualuqer mudança minha impediria diferenciar uma regressão introduzida pela funcionalidade de reservas de uma falha pré-existente

#### 1.3. Alteração no pom.xml
  - Teste usava o h2Database, porém no pom.xml apenas era declarava o postgreSQL, usado em produção.

#### 1.4. .with(authentication()) não funcionava com addFilters = false
  - O que foi encontrado: SecurityMockMvcRequestPostProcessors.authentication(...) só grava o SecurityContext na sessão mock, quem promove isso para o SecurityContextHolder durante a requisição é um filtro do Spring Security. Como todos esses testes usam @AutoConfigureMockMvc(addFilters = false), esse filtro nunca roda, e o parâmetro Authentication authentication dos controllers chega vazio/anônimo.
  - Decisão: Troca por .principal(...), que grava diretamente na requisição mock, sem o uso de filtros.
  - Porque corrigir: Similar ao achado 2, para poder ter uma baseline de testes já rodando de forma Ok antes de começar a adicionar novas implementações ao código



--- 
### 2 Decisões de projeto da funcionalidade
#### 2.1. Estratégia de concorrência para aprovação:
Lock pessimista, na linha da AreaComum, dentro da transação de aprovação.

   **Fluxo da seção crítica:**
   1.    Abrir transação.
   2.    Travar a `AreaComum` da reserva sendo aprovada.
   3.    Buscar reservas `APROVADA` da mesma área/data com intervalo sobreposto.
   4.    Se houver conflito, recusar sem escrever nada.
   5.    Caso contrário, marcar `APROVADA`, registrar data da decisão e confirmar a transação (o commit libera o lock).
         
**Por que o lock é na área e não na reserva:** o conflito acontece entre reservas *diferentes*. Dois administradores aprovando duas solicitações conflitantes tocam registros distintos; um `@Version` na reserva não detectaria isso. A área é o ponto comum que serializa as decisões.

**Onde o lock não é necessário:** `solicitar`, `negar` e `cancelar` não criam nem removem ocupação de forma concorrente-crítica. Várias reservas `SOLICITADA` conflitantes podem coexistir (RN-01-05), então só a aprovação precisa de exclusão mútua.

#### 2.2 Regra de conflito de intervalos

Dois intervalos da mesma área e da mesma data conflitam quando `inicioA < fimB` **e** `fimA > inicioB`. Consequências:
- Intervalos apenas adjacentes (uma termina às 10:00, outra começa às 10:00) **não** conflitam.
- Áreas diferentes nunca conflitam entre si (CA-01-14).
- Só reservas `APROVADA` ocupam disponibilidade; `SOLICITADA` aparece como pendente; `NEGADA` e `CANCELADA` não ocupam.