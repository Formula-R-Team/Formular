(function() {
    function aspect() {
        return window.innerWidth / window.innerHeight;
    }
    const camera = new THREE.PerspectiveCamera(50, aspect(), 0.01, 100);
    camera.position.set(10, 5, 0);
    const scene = new THREE.Scene();
//    scene.add(new THREE.GridHelper(10, 14, '#000000', '#636363'));

//    const phoneImageWidth = 480.0;
//    const phoneImageHeight = 640.0;
//    const phone = new THREE.PerspectiveCamera(50.850555, phoneImageWidth / phoneImageHeight, 1, 10);
//    phone.position.set(10, 5, 0);
//    phone.up.set(0, 1, 0);
//    phone.lookAt(0, 0, 0);
//    scene.add(phone);
//    scene.add(new THREE.CameraHelper(phone));

    const planeObj = (() => {
        const map = new THREE.TextureLoader().load('images/track-01.png');
        map.anisotropy = 16;
        const mesh = new THREE.Mesh(
            new THREE.PlaneBufferGeometry(8, 8),
            new THREE.MeshBasicMaterial({ side: THREE.DoubleSide, map: map })
        );
        mesh.lookAt(new THREE.Vector3(0.0, 1.0, 0.0));
        return mesh;
    })();
    scene.add(planeObj);
    planeObj.add(new THREE.AxesHelper());

    const displayCanvas = document.getElementById('display');
    const displayGl = displayCanvas.getContext('webgl');
    const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true, canvas: displayCanvas, context: displayGl });
    renderer.setPixelRatio(window.devicePixelRatio);
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.setClearColor(0x000000, 0.0);
    document.body.appendChild(renderer.domElement);

    const output = (() => {
        const canvas = document.getElementById('output');
        const size = canvas.width;
        const ctx = canvas.getContext('2d');
        const range = size / 2;
        return (callback) => {
                const data = ctx.createImageData(size, size);
                const buf = data.data;
                for (let x = 0; x < size; x++) {
                    for (let y = 0; y < size; y++) {
                        const color = callback((x - range) / range, -(y - range) / range);
                        const idx = 4 * (x + y * size);
                        buf[idx + 0] = color[0];
                        buf[idx + 1] = color[1];
                        buf[idx + 2] = color[2];
                        buf[idx + 3] = color[3];
                    }
                }
                ctx.putImageData(data, 0, 0);
            };
    })();

    new THREE.OrbitControls(camera, renderer.domElement);
    window.addEventListener('resize', () => {
            camera.aspect = aspect();
            camera.updateProjectionMatrix();
            renderer.setSize(window.innerWidth, window.innerHeight);
        });
    const coords = new THREE.Vector2();
    window.addEventListener('mousemove', (event) => {
            coords.set(
                2.0 * event.clientX / window.innerWidth - 1.0,
                1.0 - 2.0 * event.clientY / window.innerHeight
            );
        });
    (function animate() {
        requestAnimationFrame(animate);
        renderer.render(scene, camera);

        const plane = new THREE.Plane().setFromNormalAndCoplanarPoint(
            new THREE.Vector3(0.0, 0.0, 1.0).applyQuaternion(planeObj.quaternion),
            planeObj.position
        );

        const rayOrigin = new THREE.Vector3().setFromMatrixPosition(camera.matrixWorld);
        const rayDirection = new THREE.Vector3().set(coords.x, coords.y, 0.5).unproject(camera).sub(rayOrigin).normalize();
        const ray = new THREE.Ray(rayOrigin, rayDirection);

        const hit = new THREE.Vector3();//ray.intersectPlane(plane, new THREE.Vector3());
        const scale = 4;
        if (hit) {
            const transform = (p) => {
                const ndc = planeObj.localToWorld(p).add(hit).project(camera);
                return new THREE.Vector2(
                    ((1.0 + ndc.x) * window.innerWidth * window.devicePixelRatio / 2.0) | 0,
                    ((1.0 - ndc.y) * window.innerHeight * window.devicePixelRatio / 2.0) | 0
                );
            };
            const display = (() => {
                const canvas = renderer.domElement;
                const box = new THREE.Box2().setFromPoints([
                       transform(new THREE.Vector3(-scale, -scale, 0)),
                       transform(new THREE.Vector3(-scale,  scale, 0)),
                       transform(new THREE.Vector3( scale,  scale, 0)),
                       transform(new THREE.Vector3( scale, -scale, 0))
                   ]);
                const size = box.getSize(new THREE.Vector2());
                const buf = new Uint8Array(4 * size.width * size.height);
                displayGl.readPixels(box.min.x, canvas.height - size.height - box.min.y, size.width, size.height, displayGl.RGBA, displayGl.UNSIGNED_BYTE, buf);
                for (let y = 0; y < (size.height / 2) | 0; y++) {
                    for (let x = 0; x < size.width; x++) {
                        for (let n = 0; n < 4; n++) {
                            const i0 = 4 * (x + y * size.width) + n;
                            const i1 = 4 * (x + (size.height - 1 - y) * size.width) + n;
                            const tmp = buf[i0];
                            buf[i0] = buf[i1];
                            buf[i1] = tmp;
                        }
                    }
                }
                const transparent = [ 0, 0, 0, 0 ];
                return (pos) => {
                    if (box.containsPoint(pos)) {
                        pos.sub(box.min);
                        const idx = 4 * (pos.x + pos.y * size.width);
                        return [ buf[idx + 0], buf[idx + 1], buf[idx + 2], buf[idx + 3] ];
                    }
                    return transparent;
                };
            })();

            output((x, y) => {
                return display(transform(new THREE.Vector3(x * scale, y * scale, 0.0)));
            });
        }
    })();
})();
