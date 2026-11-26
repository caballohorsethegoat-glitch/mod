package com.creeperpastel.client;

import com.creeperpastel.entity.DroneEntity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

/** Modelo del dron (Dron.bbmodel horneado en dron_quads.json). Las helices giran. */
public class DroneModel extends EntityModel<DroneEntity> {
    private static final String DATA = "/assets/creeperpastel/models/dron_quads.json";

    private static final class Bone {
        float px, py, pz;
        float[][] quads;
        float offsetY;
        float rotY;
    }

    private final Map<String, Bone> bones = new LinkedHashMap<>();

    public DroneModel() {
        super();
        try (InputStream in = DroneModel.class.getResourceAsStream(DATA)) {
            if (in == null) throw new IllegalStateException("No se encontro " + DATA);
            JsonObject root = new Gson().fromJson(new InputStreamReader(in), JsonObject.class);
            for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("bones").entrySet()) {
                JsonObject o = e.getValue().getAsJsonObject();
                Bone bone = new Bone();
                JsonArray pv = o.getAsJsonArray("pivot");
                bone.px = pv.get(0).getAsFloat();
                bone.py = pv.get(1).getAsFloat();
                bone.pz = pv.get(2).getAsFloat();
                JsonArray qs = o.getAsJsonArray("quads");
                bone.quads = new float[qs.size()][];
                for (int i = 0; i < qs.size(); i++) {
                    JsonArray q = qs.get(i).getAsJsonArray();
                    float[] arr = new float[q.size()];
                    for (int j = 0; j < arr.length; j++) arr[j] = q.get(j).getAsFloat();
                    bone.quads[i] = arr;
                }
                bones.put(e.getKey(), bone);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error cargando el modelo del dron", ex);
        }
    }

    @Override
    public void setAngles(DroneEntity entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        float spin = animationProgress * 1.3f;
        int i = 0;
        for (Map.Entry<String, Bone> e : bones.entrySet()) {
            if (e.getKey().startsWith("prop_")) {
                e.getValue().rotY = (i++ % 2 == 0) ? spin : -spin;
            }
        }
        Bone body = bones.get("body");
        if (body != null) body.offsetY = (float) Math.sin(animationProgress * 0.15f) * 0.02f;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        // deshace el scale(-1,-1,1) + translate(0,-1.501,0) del LivingEntityRenderer (nuestros datos son Y hacia arriba)
        matrices.push();
        matrices.translate(0.0f, 1.501f, 0.0f);
        matrices.scale(-1.0f, -1.0f, 1.0f);

        boolean first = true;
        for (Bone bone : bones.values()) {
            matrices.push();
            // las helices se mueven con el cuerpo
            float oy = bones.get("body") != null ? bones.get("body").offsetY : 0f;
            matrices.translate(0.0f, oy, 0.0f);
            if (bone.rotY != 0.0f) {
                matrices.translate(bone.px, bone.py, bone.pz);
                matrices.multiply(new Quaternionf().rotationY(bone.rotY));
                matrices.translate(-bone.px, -bone.py, -bone.pz);
            }
            MatrixStack.Entry entry = matrices.peek();
            Vector3f pos = new Vector3f();
            Vector3f nor = new Vector3f();
            for (float[] q : bone.quads) {
                entry.transformNormal(q[20], q[21], q[22], nor);
                for (int v = 0; v < 4; v++) {
                    int o = v * 5;
                    entry.getPositionMatrix().transformPosition(q[o], q[o + 1], q[o + 2], pos);
                    vertices.vertex(pos.x, pos.y, pos.z, color, q[o + 3], q[o + 4], overlay, light,
                            nor.x, nor.y, nor.z);
                }
            }
            matrices.pop();
        }
        matrices.pop();
    }
}
