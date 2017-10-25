package com.rmxue.concurrent.base;

public enum ProceePriority {
    ONE {
        @Override
        public int getDBVal() {
            return 1;
        }
    }, TWO {
        @Override
        public int getDBVal() {
            return 2;
        }
    }, THREE {
        @Override
        public int getDBVal() {
            return 3;
        }
    }, FOUR {
        @Override
        public int getDBVal() {
            return 4;
        }
    };

    public abstract int getDBVal();
}
