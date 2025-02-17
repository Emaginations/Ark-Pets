/** Copyright (c) 2022, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.utils;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;


public class Plane {
    public Vector2 position;
    public Vector2 speed;
    public Vector2 speedLimit;
    public ArrayList<Vector3> barriers;
    private Vector2 world;
    private Vector2 obj;
    private float bounce = 0;
    private float gravity = 0;
    private float airFrict = 0;
    private float staticFrict = 0;
    private boolean dropped = false;

    /** Initialize a plane with gravity field.
     * The origin of coordinates (0,0) is the left-bottom point,
     * and minus(-) is allowed.
     * @param $worldWidth The width of the plane (px).
     * @param $worldHieght The height of the plane (px).
     * @param $gravity The acceleration of gravity (px/s^2).
     */
    public Plane(int $worldWidth, int $worldHeight, float $gravity) {
        position = new Vector2(0, 0);
        speed = new Vector2(0, 0);
        speedLimit = new Vector2(0, 0);
        barriers = new ArrayList<Vector3>();
        world = new Vector2($worldWidth, $worldHeight);
        obj = new Vector2(0, 0);
        bounce = 0;
        gravity = $gravity;
        airFrict = 0;
        staticFrict = 0;
    }

    /** Initialize a plane with gravity field.
     * The origin of coordinates (0,0) is the left-bottom point,
     * and minus(-) is allowed.
     * @param $worldWidth The width of the plane (px).
     * @param $worldHieght The height of the plane (px).
     */
    public Plane(int $worldWidth, int $worldHeight) {
        this($worldWidth, $worldHeight, 0);
    }

    /** Set the bounce coefficient.
     * @param $bounce The ratio of Ek to be reserved after the bounce.
     */
    public void setBounce(float $bounce) {
        bounce = $bounce > 1 ? 1 : ($bounce < 0 ? 0 : $bounce);
    }

    /** Set the frictions.
     * @param $airFrict The acceleration of air friction (px/s^2).
     * @param $staticFrict The acceleration of static friction provided by the groud (px/s^2).
     */
    public void setFrict(float $airFrict, float $staticFrict) {
        airFrict = $airFrict;
        staticFrict = $staticFrict;
    }

    /** Set the size of the object.
     * Minus(-) is allowed.
     * @param $objWidth The width of the object (px).
     * @param $objHeight The height of the object (px).
     */
    public void setObjSize(float $objWidth, float $objHeight) {
        obj.set($objWidth, $objHeight);
    }

    /** Set the limitation of speed, 0=unlimited.
     * @param $x Max speed in x-direction (px/s).
     * @param $y Max speed in y-direction (px/s).
     */
    public void setSpeedLimit(float $x, float $y) {
        speedLimit.set($x, $y);
    }

    /** Forcedly change the position of the object,
     * which will cause velocity change.
     * @param $deltaTime Delta time (s).
     * @param $x New x-position (px).
     * @param $y New y-position (px).
     */
    public void changePosition(float $deltaTime, float $x, float $y) {
        speed.set(($x-position.x)/$deltaTime, ($y-position.y)/$deltaTime);
        position.set($x, $y);
        position.set(limitX($x), limitY($y));
    }

    /** Update the position of the object.
     * @param $deltaTime Delta time (s).
     */
    public void updatePosition(float $deltaTime) {
        updateVelocity($deltaTime);
        float deltaX = speed.x * $deltaTime;
        float deltaY = speed.y * $deltaTime;
        if (position.y != borderBottom() && limitY(deltaY + position.y) == borderBottom()) {
            dropped = true;
            speed.y = 0;
        }
        // System.out.println("Y:delta"+deltaY+",speed"+speed.y+",bottom"+borderBottom()+",want"+(deltaY+position.y)+",limit"+limitY(deltaY + position.y));
        position.set(limitX(deltaX + position.x), limitY(deltaY + position.y));
    }

    /** Set a line barrier that can support the object.
     * @param $posTop The y-position of the barrier.
     * @param $posLeft The x-position of the barrier's left edge.
     * @param $posRight The x-position of the barrier's right edge.
     * @param $overCover Whether to set the highest priority to this barrier.
     */
    public void setBarrier(float $posTop, float $posLeft, float $width, boolean $overCover) {
        if ($overCover)
            barriers.add(0, new Vector3($posLeft, $posTop, $width));
        else
            barriers.add(new Vector3($posLeft, $posTop, $width));
    }

    /** Get the x-position of the object.
     * @return X (px).
     */
    public float getX() {
        return position.x;
    }

    /** Get the y-position of the object.
     * @return Y (px).
     */
    public float getY() {
        return position.y;
    }

    /** Get the dropped-status of the object.
     * @return true=dropped.
     */
    public boolean getDropped() {
        if (dropped) {
            dropped = false; // Reset
            return true;
        }
        return false;
    }

    /** Get the dropping-status of the object.
     * @return true=dropping.
     */
    public boolean getDropping() {
        if (position.y != borderBottom())
            return true;
        return false;
    }

    /** Update the velocity of the object.
     * @param $deltaTime Delta time (s).
     */
    private void updateVelocity(float $deltaTime) {
        final float BOTTOM = borderBottom();
        final float TOP = borderTop();
        // Gravity
        speed.y -= gravity * $deltaTime;
        if (position.y == BOTTOM) {
            speed.y = 0;
        }
        else if (position.y == TOP && speed.y > 0)
            speed.y = 0;
        // Ground friction
        if (position.y == BOTTOM)
            speed.x = applyFriction(speed.x, staticFrict, $deltaTime);
        // Air friction
        speed.x = applyFriction(speed.x, airFrict, $deltaTime);
        speed.y = applyFriction(speed.y, airFrict, $deltaTime);
        // Limit
        if (speedLimit.x != 0 && Math.abs(speed.x) > speedLimit.x)
            speed.x = Math.signum(speed.x) * speedLimit.x;
        if (speedLimit.y != 0 && Math.abs(speed.y) > speedLimit.y)
            speed.y = Math.signum(speed.y) * speedLimit.y;
        // Bounce
        if (bounce != 0 && (position.x == borderLeft() || position.x == borderRight())) {
            speed.x = (float)(Math.sqrt(speed.x * speed.x * bounce) * Math.signum(-speed.x));
        }
    }

    /** Apply a friction to a velocity.
     * @param $speed The velocity (px/s).
     * @param $frict The acceleration of friction (px/s^2).
     * @param $deltaTime Delta time (s).
     * @return New velocity (px/s).
     */
    private float applyFriction(float $speed, float $firct, float $deltaTime) {
        float delta = Math.signum($speed) * $firct * $deltaTime;
        return Math.signum($speed - delta) == Math.signum(delta) ? $speed - delta : 0;
    }

    /** Limit the x-position to avoid overstepping.
     * @param $x X (px).
     * @return New x (px).
     */
    private float limitX(float $x) {
        return $x<borderLeft() ? borderLeft() : ($x>borderRight() ? borderRight() : $x);
    }

    /** Limit the y-position to avoid overstepping.
     * @param $x Y (px).
     * @return New y (px).
     */
    private float limitY(float $y) {
        return $y<borderBottom() ? borderBottom() : ($y>borderTop() ? borderTop() : $y);
    }

    /** Get the border position of the top.
     * @return Y (px).
     */
    private float borderTop() {
        if (world.y > 0)
            return obj.y < 0 ? world.y : world.y - obj.y;
        else
            return obj.y < 0 ? 0 : -obj.y;
    }

    /** Get the border position of the bottom.
     * @return Y (px).
     */
    private float borderBottom() {
        for (Vector3 i : barriers) {
            if (i.x <= position.x-obj.x && position.x <= i.x+i.z)
                if (Math.abs(position.y) <= Math.abs(i.y))
                    return obj.y < 0 ? i.y - obj.y : i.y;
        }
        if (world.y > 0)
            return obj.y < 0 ? -obj.y : 0;
        else
            return obj.y < 0 ? world.y - obj.y : world.y;
    }

    /** Get the border position of the right.
     * @return X (px).
     */
    private float borderRight() {
        if (world.x > 0)
            return obj.x < 0 ? world.x : world.x - obj.x;
        else
            return obj.x < 0 ? 0 : -obj.x;
    }

    /** Get the border position of the left.
     * @return X (px).
     */
    private float borderLeft() {
        if (world.x > 0)
            return obj.x < 0 ? -obj.x : 0;
        else
            return obj.x < 0 ? world.x - obj.x : world.x;
    }
}
