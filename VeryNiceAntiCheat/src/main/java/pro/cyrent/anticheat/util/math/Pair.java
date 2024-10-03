package pro.cyrent.anticheat.util.math;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Pair<K, V> {
    K k;
    V v;
}