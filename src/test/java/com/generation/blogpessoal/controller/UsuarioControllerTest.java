package com.generation.blogpessoal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.model.UsuarioLogin;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.service.UsuarioService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsuarioControllerTest {

	/*  anotação @SpringBootTest indica que a Classe UsuarioControllerTest é uma Classe Spring Boot Testing. 
	 * 	A Opção environment indica que caso a porta principal (8080 para uso local) esteja ocupada,
	 *  o Spring irá atribuir uma outra porta automaticamente.
	 *a anotação @TestInstance indica que o Ciclo de vida da Classe de Teste será por Classe.
	 */
	
	@Autowired
	private TestRestTemplate testRestTemplate;
	/* foi injetado (@Autowired), um objeto da Classe TestRestTemplate para enviar as requisições para a nossa aplicação.
	  */

	@Autowired
	private UsuarioService usuarioService;
	/* foi injetado (@Autowired), um objeto da Classe UsuarioService 
	 * para persistir os objetos no Banco de dados de testes com a senha criptografada. 
	 */
	@Autowired
	private UsuarioRepository usuarioRepository;
	/* foi injetado (@Autowired), um objeto da Interface UsuarioRepository
	 *  para limpar o Banco de dados de testes.
	 */
	
	
	
	@BeforeAll
	void start(){

		usuarioRepository.deleteAll();

		usuarioService.cadastrarUsuario(new Usuario(0L, 
			"Root", "root@root.com", "rootroot", "-"));

	}
	/* Método start(), anotado com a anotação @BeforeAll, apaga todos os dados da tabela e 
	 * cria o usuário root@root.com para testar os Métodos protegidos por autenticação.
	 * 
	 */

	@Test
	@DisplayName("Cadastrar Um Usuário")
	public void deveCriarUmUsuario() {
		/* Método deveCriarUmUsuario() 
		 * foi anotado com a anotação @Test que indica que este Método executará um teste.
		 * a anotação @DisplayName configura uma mensagem que será exibida ao invés do nome do Método
		 */

		HttpEntity<Usuario> corpoRequisicao = new HttpEntity<Usuario>(new Usuario(0L, 
			"Paulo Antunes", "paulo_antunes@email.com.br", "13465278", "-"));
		/* foi criado um objeto da Classe HttpEntity chamado corpoRequisicao,
		 *  recebendo um objeto da Classe Usuario.
		 */

		ResponseEntity<Usuario> corpoResposta = testRestTemplate
			.exchange("/usuarios/cadastrar", HttpMethod.POST, corpoRequisicao, Usuario.class);
		/* a Requisição HTTP será enviada através do Método exchange()
		 *  da Classe TestRestTemplate e a Resposta da Requisição (Response)
		 * será recebida pelo objeto corpoResposta do tipo ResponseEntity
		 */
		assertEquals(HttpStatus.CREATED, corpoResposta.getStatusCode());
		/* através do Método de asserção AssertEquals(), checaremos se a resposta da requisição (Response)
		 * Para obter o status da resposta vamos utilizar o Método getStatusCode() da Classe ResponseEntity.

		 */
		
	}
	@Test
	@DisplayName("😠Não deve permitir duplicação do Usuário")
	public void naoDeveDuplicarUsuario() {

		usuarioService.cadastrarUsuario(new Usuario(0L, 
			"Maria da Silva", "maria_silva@email.com.br", "13465278", "-"));
/*através do Método cadastrarUsuario() da Classe UsuarioService,
 *  foi persistido um Objeto da Classe Usuario no Banco de dados
 * 
 */
		HttpEntity<Usuario> corpoRequisicao = new HttpEntity<Usuario>(new Usuario(0L, 
			"Maria da Silva", "maria_silva@email.com.br", "13465278", "-"));
/* foi criado um objeto HttpEntity chamado corpoRequisicao,
 * cebendo um objeto da Classe Usuario contendo os mesmos dados do objeto persistido
 */
		ResponseEntity<Usuario> corpoResposta = testRestTemplate
			.exchange("/usuarios/cadastrar", HttpMethod.POST, corpoRequisicao, Usuario.class);
		/* Requisição HTTP será enviada através do Método exchange() 
		 * da Classe TestRestTemplate e a Resposta da Requisição (Response)
		 * será recebida pelo objeto corpoResposta do tipo ResponseEntity
		 * Para enviar a requisição, o será necessário passar 4 parâmetros: URL,Método HTTP,Objeto HttpEntity
		 * e conteúdo esperado no Corpo da Resposta (Response Body)
		 */

		assertEquals(HttpStatus.BAD_REQUEST, corpoResposta.getStatusCode());
		/* através do Método de asserção AssertEquals(), checaremos se a resposta da requisição (Response)
		 *  Para obter o status da resposta vamos utilizar o Método getStatusCode() da Classe ResponseEntity.
		 * 
		 */
	}

	@Test
	@DisplayName("🌍Atualizar um Usuário")
	public void deveAtualizarUmUsuario() {

		Optional<Usuario> usuarioCadastrado = usuarioService.cadastrarUsuario(new Usuario(0L, 
			"Juliana Andrews", "juliana_andrews@email.com.br", "juliana123", "-"));
		/* foi criado um Objeto Optional, do tipo Usuario, chamado usuarioCreate
		 * para armazenar o resultado da persistência de um Objeto da Classe Usuario no Banco de dados
		 * através do Método cadastrarUsuario() da Classe UsuarioService
		 *  método "cadastrarUsuario" pode retornar um Optional vazio se houver algum erro no cadastro do usuário,
		 *   em vez de retornar null ou lançar uma exceção
		 */

		Usuario usuarioUpdate = new Usuario(usuarioCadastrado.get().getId(), 
			"Juliana Andrews Ramos", "juliana_ramos@email.com.br", "juliana123" , "-");
		/* foi criado um Objeto do tipo Usuario, chamado usuarioUpdate,
		 * que será utilizado para atualizar os dados persistidos no Objeto usuarioCreate
		 */
		
		HttpEntity<Usuario> corpoRequisicao = new HttpEntity<Usuario>(usuarioUpdate);
		/*foi criado um objeto HttpEntity chamado corpoRequisicao,
		 *  recebendo o objeto da Classe Usuario chamado usuarioUpdate
		 * 
		 */

		ResponseEntity<Usuario> corpoResposta = testRestTemplate
			.withBasicAuth("root@root.com", "rootroot")
			.exchange("/usuarios/atualizar", HttpMethod.PUT, corpoRequisicao, Usuario.class);
		/*  deverá efetuar o login com um usuário e uma senha válida para realizar os testes.
		 * 
		 */
		
		assertEquals(HttpStatus.OK, corpoResposta.getStatusCode());
		
	}

	@Test
	@DisplayName("😉Listar todos os Usuários")
	public void deveMostrarTodosUsuarios() {

		usuarioService.cadastrarUsuario(new Usuario(0L, 
			"Sabrina Sanches", "sabrina_sanches@email.com.br", "sabrina123", "-"));
		
		usuarioService.cadastrarUsuario(new Usuario(0L, 
			"Ricardo Marques", "ricardo_marques@email.com.br", "ricardo123", "-"));
		/* foram persistidos dois Objetos da Classe Usuario no Banco de dados, 
		 * através do Método cadastrarUsuario() da Classe UsuarioService.
		 * 
		 */

		ResponseEntity<String> resposta = testRestTemplate
		.withBasicAuth("root@root.com", "rootroot")
			.exchange("/usuarios/all", HttpMethod.GET, null, String.class);
		/* deverá efetuar o login com um usuário e uma senha válida para realizar os testes.
		 * 
		 */
		assertEquals(HttpStatus.OK, resposta.getStatusCode());

}
	@Test
	@DisplayName("😬Listar Um Usuário Específico")
	public void deveListarApenasUmUsuario() {
	
		Optional<Usuario> usuarioBusca = usuarioService.cadastrarUsuario(new Usuario(0L, 
				"Laura Santolia", "laura_santolia@email.com.br", "laura12345", "-"));
			
		ResponseEntity<String> resposta = testRestTemplate
				.withBasicAuth("root@root.com", "rootroot")
				.exchange("/usuarios/" + usuarioBusca.get().getId(), HttpMethod.GET, null, String.class);

		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		
	}

	@Test
	@DisplayName("😮Login do Usuário")
	public void deveAutenticarUsuario() {

		usuarioService.cadastrarUsuario(new Usuario(0L, 
			"Marisa Souza", "marisa_souza@email.com.br", "13465278", "-"));

		HttpEntity<UsuarioLogin> corpoRequisicao = new HttpEntity<UsuarioLogin>(new UsuarioLogin(0L,"", "marisa_souza@email.com.br", "13465278", "", ""));

		ResponseEntity<UsuarioLogin> corpoResposta = testRestTemplate
			.exchange("/usuarios/logar", HttpMethod.POST, corpoRequisicao, UsuarioLogin.class);

		assertEquals(HttpStatus.OK, corpoResposta.getStatusCode());

	}

}
