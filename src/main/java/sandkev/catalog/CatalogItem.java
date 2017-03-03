package sandkev.catalog;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import sandkev.shared.dao.Identifiable;

/**
 * Created by kevin on 10/05/2015.
 */
public class CatalogItem implements Identifiable<Long> {
    private long id;
    private final String name;
    private final String season;
    private final String magazine;
    private final String article;

    public CatalogItem(String name, String season, String magazine, String article) {
        this(-1,name,season,magazine,article);
    }
    public CatalogItem(long id,String name, String season, String magazine, String article) {
        this.id = id;
        this.name = name;
        this.season = season;
        this.magazine = magazine;
        this.article = article;
    }

    //@Override
    public Long getId() {
        return id;
    }

    //@Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getSeason() {
        return season;
    }

    public String getMagazine() {
        return magazine;
    }

    public String getArticle() {
        return article;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CatalogItem that = (CatalogItem) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(name, that.name)
                .append(season, that.season)
                .append(magazine, that.magazine)
                .append(article, that.article)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(season)
                .append(magazine)
                .append(article)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "CatalogItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", season='" + season + '\'' +
                ", magazine='" + magazine + '\'' +
                ", article='" + article + '\'' +
                '}';
    }
}
