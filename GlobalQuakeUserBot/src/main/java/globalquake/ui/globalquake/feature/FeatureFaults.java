package globalquake.ui.globalquake.feature;

import globalquake.core.faults.GQLine;
import globalquake.ui.globe.GlobeRenderer;
import globalquake.ui.globe.Point2D;
import globalquake.ui.globe.Polygon3D;
import globalquake.ui.globe.RenderProperties;
import globalquake.ui.globe.feature.RenderElement;
import globalquake.ui.globe.feature.RenderEntity;
import globalquake.ui.globe.feature.RenderFeature;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public class FeatureFaults extends RenderFeature<GQLine> {
    private static final AlphaComposite TRANSPARENT = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
    public static final Color borderColor = new Color(81, 110, 174);

    private final List<GQLine> polygonList;
    private final double minScroll;
    private final double maxScroll;

    private boolean enabled = true;

    public FeatureFaults(List<GQLine> polygonList, double minScroll, double maxScroll){
        super(1);
        this.polygonList = polygonList;
        this.minScroll = minScroll;
        this.maxScroll = maxScroll;
    }

    @Override
    public Collection<GQLine> getElements() {
        return polygonList;
    }

    @Override
    public boolean isEnabled(RenderProperties properties) {
        return enabled && properties.scroll >= minScroll && properties.scroll < maxScroll;
    }

    @Override
    public boolean needsUpdateEntities() {
        return false;
    }

    @Override
    public boolean needsProject(RenderEntity<GQLine> entity, boolean propertiesChanged) {
        return propertiesChanged;
    }

    @Override
    public boolean needsCreatePolygon(RenderEntity<GQLine> entity, boolean propertiesChanged) {
        return false;
    }

    @Override
    public void createPolygon(GlobeRenderer renderer, RenderEntity<GQLine> entity, RenderProperties renderProperties) {
        Polygon3D result_pol = new Polygon3D();

        for(int i = 0; i < entity.getOriginal().getSize(); i++){
            result_pol.addPoint(GlobeRenderer.createVec3D(new Point2D(entity.getOriginal().getLats()[i], entity.getOriginal().getLons()[i])));
        }
        result_pol.finish();
        entity.getRenderElement(0).setPolygon(result_pol);
    }

    @Override
    public void project(GlobeRenderer renderer, RenderEntity<GQLine> entity, RenderProperties renderProperties) {
        RenderElement element = entity.getRenderElement(0);
        element.getShape().reset();
        element.shouldDraw = renderer.project3DSimple(element.getShape(), element.getPolygon(), true, renderProperties);
    }

    @Override
    public Point2D getCenterCoords(RenderEntity<GQLine> entity) {
        return null;
    }

    @Override
    public void render(GlobeRenderer renderer, Graphics2D graphics, RenderEntity<GQLine> entity, RenderProperties renderProperties) {
        RenderElement element = entity.getRenderElement(0);
        if(!element.shouldDraw){
            return;
        }
        graphics.setComposite(TRANSPARENT);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setColor(borderColor);
        graphics.draw(element.getShape());

        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
