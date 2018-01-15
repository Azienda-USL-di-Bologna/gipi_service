package it.bologna.ausl.gipi.odata.complextypes;

import it.bologna.ausl.entities.baborg.Struttura;
import it.nextsw.olingo.edmextension.annotation.EdmSimpleProperty;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.SqlResultSetMapping;
import org.apache.olingo.odata2.api.annotation.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.springframework.stereotype.Component;

@EdmComplexType
@Component
@SqlResultSetMapping(
        name = "Test",
        classes = {
            @ConstructorResult(
                    targetClass = Test.class,
                    columns = {
                        @ColumnResult(name = "prova")
                    }
            )
        }
)
public class Test {

    @EdmSimpleProperty(type = EdmSimpleTypeKind.String)
    String prova;

    public Test() {
        super();
        prova = "ciao";
    }

    public String getProva() {
        return prova;
    }

    public void setProva(String prova) {
        this.prova = prova;
    }

}
