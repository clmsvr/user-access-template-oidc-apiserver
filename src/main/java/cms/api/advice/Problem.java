package cms.api.advice;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;


@JsonInclude(Include.NON_NULL)
@Getter
@Setter
//@Schema(name = SchemaNames.PROBLEMA)
public class Problem {

	//@Schema(example = "https://algafood.com.br/dados-invalidos")
	private String type;
	
	//@Schema(example = "Dados inválidos")
	private String title;
	
	//@Schema(example = "400")
	private int status;
	
	//@Schema(example = "Um ou mais campos estão inválidos.")
	private String detail;
	
	//@Schema(example = "2007-12-03T10:15:30+01:00")
	private OffsetDateTime timestamp;
	
	//nulo nao eh serializado
	//@Schema(description = "Lista de campos que geraram o erro")
	private List<Field> fields;
	
	
	public Problem(ProblemTypeTitleStatus type, String detail) 
	{
		this(type, detail, null);
	}
	
	public Problem(ProblemTypeTitleStatus type, String detail, List<Field> fields) 
	{
		this.type = type.getType();
		this.title = type.getTitle();
		this.status = type.getStatus().value();
		this.detail = detail;
		this.timestamp = OffsetDateTime.now();
		this.fields = fields;
	}
	
	public Problem(String title, int status) 
	{
		this.title = title;
		this.status = status;
		this.timestamp = OffsetDateTime.now();
	}

	@Getter
	@Setter
	//@Schema(name = SchemaNames.FIELD_ERROR)
	public static class Field {
		
		//@Schema(example = "preco")
		private String name;
		
		//@Schema(example = "O preço é inválido")
		private String message;

		
		public Field(String name, String message) 
		{
			this.name = name;
			this.message = message;
		}
	}	
	
}
