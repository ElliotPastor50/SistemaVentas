package dto;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Naomi Alejandra Vega
 */
@Entity
@Table(name = "kardex")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Kardex.findAll", query = "SELECT k FROM Kardex k"),
    @NamedQuery(name = "Kardex.findByIdKardex", query = "SELECT k FROM Kardex k WHERE k.idKardex = :idKardex"),
    @NamedQuery(name = "Kardex.findByMoviKard", query = "SELECT k FROM Kardex k WHERE k.moviKard = :moviKard"),
    @NamedQuery(name = "Kardex.findByCantProd", query = "SELECT k FROM Kardex k WHERE k.cantProd = :cantProd")})
public class Kardex implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "IdKardex")
    private Integer idKardex;
    @Basic(optional = false)
    @NotNull
    @Column(name = "MoviKard")
    private int moviKard;
    @Basic(optional = false)
    @NotNull
    @Column(name = "cantProd")
    private int cantProd;
    @JoinColumn(name = "IdProducto", referencedColumnName = "IdProducto")
    @ManyToOne(optional = false)
    private Productos idProducto;

    public Kardex() {
    }

    public Kardex(Integer idKardex) {
        this.idKardex = idKardex;
    }

    public Kardex(Integer idKardex, int moviKard, int cantProd) {
        this.idKardex = idKardex;
        this.moviKard = moviKard;
        this.cantProd = cantProd;
    }

    public Integer getIdKardex() {
        return idKardex;
    }

    public void setIdKardex(Integer idKardex) {
        this.idKardex = idKardex;
    }

    public int getMoviKard() {
        return moviKard;
    }

    public void setMoviKard(int moviKard) {
        this.moviKard = moviKard;
    }

    public int getCantProd() {
        return cantProd;
    }

    public void setCantProd(int cantProd) {
        this.cantProd = cantProd;
    }

    public Productos getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Productos idProducto) {
        this.idProducto = idProducto;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idKardex != null ? idKardex.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Kardex)) {
            return false;
        }
        Kardex other = (Kardex) object;
        if ((this.idKardex == null && other.idKardex != null) || (this.idKardex != null && !this.idKardex.equals(other.idKardex))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "dto.Kardex[ idKardex=" + idKardex + " ]";
    }
    
}
