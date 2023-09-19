// ISpooferService.aidl
package project.listick.fakegps.Services;

import project.listick.fakegps.MultipleRoutesInfo;

interface ISpooferService {
    void attachRoutes(inout List<MultipleRoutesInfo> points);
    void setPause(boolean pause);
    boolean isPaused();
    List<MultipleRoutesInfo> getRoutes();
}
