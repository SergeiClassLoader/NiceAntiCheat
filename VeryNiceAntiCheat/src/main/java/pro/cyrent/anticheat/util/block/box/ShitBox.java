package pro.cyrent.anticheat.util.block.box;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @AllArgsConstructor @Setter
public class ShitBox {
    private double value;

    public void set(double lol) {
        this.value = lol;
    }

    public double get() {
        return this.value;
    }
}
