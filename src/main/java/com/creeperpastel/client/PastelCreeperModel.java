package com.creeperpastel.client;

import com.creeperpastel.entity.PastelCreeperEntity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Modelo del Creeper Pastel exportado desde Blockbench (CreeperPastel.bbmodel).
 * Los cubos ya vienen "horneados" como quads con UV por cara en
 * assets/creeperpastel/models/creeper_pastel_quads.json (coordenadas en bloques, Y hacia arriba).
 *
 * Animaciones del .bbmodel:
 *  - idle: el hueso "cuerpo" baja 0.3 px y sube, ciclo de 1.5 s
 *  - walk: pie1/pie3 y pie2/pie4 rotan +-5 grados en X (en contrafase)
 */
public class PastelCreeperModel extends EntityModel<PastelCreeperEntity> {
    private static final String DATA = "/assets/creeperpastel/models/creeper_pastel_quads.json";

    private static final class Bone {
        float px, py, pz;          // pivote
        float[][] quads;           // 4 * (x,y,z,u,v) + (nx,ny,nz)
        float offsetY;             // animacion de posicion (bloques)
        float rotX;                // animacion de rotacion (radianes)
    }

    private final Map<String, Bone> bones = new LinkedHashMap<>();

    public PastelCreeperModel() {
        super();
        load();
    }

    private void load() {
        try (InputStream in = PastelCreeperModel.class.getResourceAsStream(DATA)) {
            if (in == null) throw new IllegalStateException("No se encontro " + DATA);
            JsonObject root = new Gson().fromJson(new InputStreamReader(in), JsonObject.class);
            JsonObject b = root.getAsJsonObject("bones");
            for (Map.Entry<String, JsonElement> e : b.entrySet()) {
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
            throw new RuntimeException("Error cargando el modelo del Creeper Pastel", ex);
        }
    }

    @Override
    public void setAngles(PastelCreeperEntity entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        // idle: triangulo 0 -> -0.3px -> 0 en 1.5 s (animationProgress esta en ticks)
        float t = (animationProgress / 20.0f) % 1.5f;
        float k = t < 0.75f ? t / 0.75f : (1.5f - t) / 0.75f;
        bones.get("cuerpo").offsetY = -0.3f / 16.0f * k;

        // walk: +-5 grados, pies en diagonal en contrafase
        float swing = MathHelper.cos(limbAngle * 0.6662f) * (float) Math.toRadians(5.0)
                * MathHelper.clamp(limbDistance * 2.0f, 0.0f, 1.0f);
        bones.get("pie1").rotX = swing;
        bones.get("pie3").rotX = swing;
        bones.get("pie2").rotX = -swing;
        bones.get("pie4").rotX = -swing;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        // LivingEntityRenderer ya aplico scale(-1,-1,1) + translate(0,-1.501,0) (espacio Y hacia abajo).
        // Nuestros datos son Y hacia arriba, asi que deshacemos esa transformacion.
        matrices.push();
        matrices.translate(0.0f, 1.501f, 0.0f);
        matrices.scale(-1.0f, -1.0f, 1.0f);

        for (Bone bone : bones.values()) {
            matrices.push();
            matrices.translate(0.0f, bone.offsetY, 0.0f);
            if (bone.rotX != 0.0f) {
                matrices.translate(bone.px, bone.py, bone.pz);
                matrices.multiply(new org.joml.Quaternionf().rotationX(bone.rotX));
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
