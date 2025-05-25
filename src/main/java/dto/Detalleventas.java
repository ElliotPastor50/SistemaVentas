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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "detalleventas")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Detalleventas.findAll", query = "SELECT d FROM Detalleventas d"),
    @NamedQuery(name = "Detalleventas.findByIdDetalleVenta", query = "SELECT d FROM Detalleventas d WHERE d.idDetalleVenta = :idDetalleVenta"),
    @NamedQuery(name = "Detalleventas.findByCantProd", query = "SELECT d FROM Detalleventas d WHERE d.cantProd = :cantProd"),
    @NamedQuery(name = "Detalleventas.findByDetallePrecio", query = "SELECT d FROM Detalleventas d WHERE d.detallePrecio = :detallePrecio"),
    @NamedQuery(name = "Detalleventas.findBySbttPrecio", query = "SELECT d FROM Detalleventas d WHERE d.sbttPrecio = :sbttPrecio")})
public class Detalleventas implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "IdDetalleVenta")
    private Integer idDetalleVenta;
    @Basic(optional = false)
    @NotNull
    @Column(name = "CantProd")
    private int cantProd;
    @Basic(optional = false)
    @NotNull
    @Column(name = "DetallePrecio")
    private double detallePrecio;
    @Basic(optional = false)
    @NotNull
    @Column(name = "SbttPrecio")
    private double sbttPrecio;
    @ManyToOne(optional = false)
    @JoinColumn(name = "idProducto", nullable = false)
    private Productos idProducto;
    @JoinColumn(name = "IdVenta", referencedColumnName = "IdVenta")
    @ManyToOne(optional = false)
    private Ventas idVenta;

    public Detalleventas() {
    }

    public Detalleventas(Integer idDetalleVenta) {
        this.idDetalleVenta = idDetalleVenta;
    }

    public Detalleventas(Integer idDetalleVenta, int cantProd, double detallePrecio, double sbttPrecio) {
        this.idDetalleVenta = idDetalleVenta;
        this.cantProd = cantProd;
        this.detallePrecio = detallePrecio;
        this.sbttPrecio = sbttPrecio;
    }

    public Integer getIdDetalleVenta() {
        return idDetalleVenta;
    }

    public void setIdDetalleVenta(Integer idDetalleVenta) {
        this.idDetalleVenta = idDetalleVenta;
    }

    public int getCantProd() {
        return cantProd;
    }

    public void setCantProd(int cantProd) {
        this.cantProd = cantProd;
    }

    public double getDetallePrecio() {
        return detallePrecio;
    }

    public void setDetallePrecio(double detallePrecio) {
        this.detallePrecio = detallePrecio;
    }

    public double getSbttPrecio() {
        return sbttPrecio;
    }

    public void setSbttPrecio(double sbttPrecio) {
        this.sbttPrecio = sbttPrecio;
    }

    public Productos getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Productos idProducto) {
        this.idProducto = idProducto;
    }

    public Ventas getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Ventas idVenta) {
        this.idVenta = idVenta;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idDetalleVenta != null ? idDetalleVenta.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Detalleventas)) {
            return false;
        }
        Detalleventas other = (Detalleventas) object;
        if ((this.idDetalleVenta == null && other.idDetalleVenta != null) || (this.idDetalleVenta != null && !this.idDetalleVenta.equals(other.idDetalleVenta))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "dto.Detalleventas[ idDetalleVenta=" + idDetalleVenta + " ]";
    }

}
