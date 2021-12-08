// ISpooferService.aidl
package project.listick.fakegps.Daemons;

import project.listick.fakegps.MultipleRoutesInfo;

interface ISpooferService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void attachRoutes(inout List<MultipleRoutesInfo> points);
    void setPause(boolean pause);
    boolean isPaused();
    List<MultipleRoutesInfo> getRoutes();
}
