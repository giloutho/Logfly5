/**
 * Written by Periklis Master_ex Ntanasis <pntanasis@gmail.com>
 * http://masterex.github.com/
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package liveUpdate.objects;

/**
 *
 * @author Periklis Ntanasis
 */
public class Instruction {

    private Action action;
    private String destination;
    private String filename;

    public Action getAction() {
        return action;
    }

    public String getDestination() {
        return destination;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setAction(String value) {
        if (value.equalsIgnoreCase("MOVE")) {
            this.action = Action.MOVE;
        } else if (value.equalsIgnoreCase("DELETE")) {
            this.action = Action.DELETE;
        } else if (value.equalsIgnoreCase("EXECUTE")) {
            this.action = Action.EXECUTE;
        }
    }
}
