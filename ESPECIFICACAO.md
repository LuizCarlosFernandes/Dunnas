- 1- Pacotes Java declarados divergentes da estrutura de pastas em todos os testes.
  - O que foi encontrado: Todos o arquivos estão fisicamentes organizandos em src/test/.../unit/... e src/test/...integration/..., mas o package declada no topo de 15 arquivos ainda apontava para o pacote da classe de produção corresponte.
  - Investigação feita antes de decidir: verificado se alguma classe de produção testada expõe os métodos, campos ou contrutores package-private dos quais os testes dependessem, como não foi encontrando nenhum caso onde o acesso é feito por membros public. 
  - Decisão: Corrigido o package declarado de cada um dos 15 arquivos para bater com sua pasta física, e adicionando o import explícito da classe de produção que antes era resolvido implicitamente por estar no mesmo pacote. Nenhum teste teve sou lógica ou asserções alteradas.

- 2- @WebMvcTest sem mock de JwtService quebrava toda a suíte de testes web
  - O que foi encontrado: ao rodar ./mvnw teste na base recebida, 22 de 56 testes falhavam antes mesmo de qualquer asserção.
  - Decisão: corrigido adicionando @MockitoBean private JwtService jwtService; 
  - Porque corrigir: um baseline de testes quebrados antes de qualuqer mudança minha impediria diferenciar uma regressão introduzida pela funcionalidade de reservas de uma falha pré-existente