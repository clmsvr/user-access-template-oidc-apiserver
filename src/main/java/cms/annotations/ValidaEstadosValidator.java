package cms.annotations;

import java.util.HashMap;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class ValidaEstadosValidator implements ConstraintValidator<ValidaEstados, String> {

	
    private static HashMap<String, String> states = new HashMap<>();
    static{
        states.put("AC","Acre");
        states.put("AL","Alagoas");
        states.put("AP","Amapá");
        states.put("AM","Amazonas");
        states.put("BA","Bahia");
        states.put("CE","Ceará");
        states.put("DF","Distrito Federal");
        states.put("ES","Espirito Santo");
        states.put("GO","Goiás");
        states.put("MA","Maranhão");
        states.put("MS","Mato Grosso do Sul");
        states.put("MT","Mato Grosso");
        states.put("MG","Minas Gerais");
        states.put("PA","Pará");
        states.put("PB","Paraíba");
        states.put("PR","Paraná");
        states.put("PE","Pernambuco");
        states.put("PI","Piauí");
        states.put("RJ","Rio de Janeiro");
        states.put("RN","Rio Grande do Norte");
        states.put("RS","Rio Grande do Sul");
        states.put("RO","Rondônia");
        states.put("RR","Roraima");
        states.put("SC","Santa Catarina");
        states.put("SP","São Paulo");
        states.put("SE","Sergipe");
        states.put("TO","Tocantins");
    }
	
	@Override
	public void initialize(ValidaEstados constraint) {
	}
	
	@Override
	public boolean isValid(String estado, ConstraintValidatorContext context) 
	{
		if (estado == null) return true;
		
		if ( states.get(estado.toUpperCase()) == null )
			return false;
		
		return true;
	}

}