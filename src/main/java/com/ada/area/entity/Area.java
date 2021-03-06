package com.ada.area.entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.quhaodian.data.entity.CatalogEntity;

/**
 * 地区
 *
 * @author 年高
 */
@Entity
@Table(name = "area")
public class Area extends CatalogEntity {

    /**
     * 下属地区
     */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Area> childrens;
    /**
     * 父地区id
     */
    @JoinColumn(name = "pid")
    @ManyToOne(fetch = FetchType.LAZY)
    private Area parent;
    /**
     * 城市状态 1为开通，0为未开通
     */
    private Integer state;

    public List<Area> getChildrens() {
        if (childrens != null && childrens.size() > 0) {
            return childrens;

        } else {
            return null;

        }
    }

    public void setChildrens(List<Area> childrens) {
        this.childrens = childrens;
    }


    public Area getParent() {
        return parent;
    }

    public void setParent(Area parent) {
        this.parent = parent;
    }

    public Integer getParentId() {
        Area parent = getParent();
        if (parent != null) {
            return parent.getId();
        } else {
            return null;
        }
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

}
