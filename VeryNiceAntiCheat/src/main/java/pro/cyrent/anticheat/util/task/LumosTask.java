package pro.cyrent.anticheat.util.task;

import pro.cyrent.anticheat.util.Callback;

public class LumosTask {
   private int id;
   private final Callback<Integer> callback;

   public LumosTask(Callback<Integer> callback) {
      this.callback = callback;
   }

   public LumosTask(Callback<Integer> callback, int id) {
      this.callback = callback;
      this.id = id;
   }

   public void runTask() {
      this.callback.call(this.id);
   }

   public int getId() {
      return this.id;
   }

   public Callback<Integer> getCallback() {
      return this.callback;
   }

   public void setId(int id) {
      this.id = id;
   }
}