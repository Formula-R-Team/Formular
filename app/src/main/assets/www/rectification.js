(function() {
    function aspect() {
        return window.innerWidth / window.innerHeight;
    }
    const camera = new THREE.PerspectiveCamera(50, aspect(), 0.01, 100);
    camera.position.set(10, 5, 0);
    const scene = new THREE.Scene();
    scene.add(new THREE.GridHelper(10, 14, '#000000', '#636363'));

    const planeObj = (() => {
        const map = new THREE.TextureLoader().load('images/box_circle.png');
        map.anisotropy = 16;
        const mesh = new THREE.Mesh(
            new THREE.PlaneBufferGeometry(5, 5),
            new THREE.MeshBasicMaterial({ side: THREE.DoubleSide, map: map })
        );
        mesh.lookAt(new THREE.Vector3(0.0, 1.0, 0.0));
        return mesh;
    })();
    scene.add(planeObj);

    const displayCanvas = document.getElementById('display');
    const displayGl = displayCanvas.getContext('webgl');
    const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true, canvas: displayCanvas, context: displayGl });
    renderer.setPixelRatio(window.devicePixelRatio);
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.setClearColor(0x000000, 0.0);
    document.body.appendChild(renderer.domElement);
    console.log(renderer.domElement);

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
    {
        const viewProjMtx = new THREE.Matrix4();
        viewProjMtx.makePerspective(-1.0, 1.0, -1.0, 1.0, 0.2, 10.0);
        const test = new THREE.Vector3(0.25, 0.0, 0.75).applyMatrix4(viewProjMtx);
        const invertedProjectionMatrix = new THREE.Matrix4().getInverse(viewProjMtx, true);
        console.log(test.applyMatrix4(invertedProjectionMatrix));
    }
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

        const hit = ray.intersectPlane(plane, new THREE.Vector3());
        const scale = 3;
        if (hit) {
            const display = (() => {
                const canvas = renderer.domElement;
                // TODO: capture only needed region
                const buf = new Uint8Array(4 * canvas.width * canvas.height);
                displayGl.readPixels(0, 0, canvas.width, canvas.height, displayGl.RGBA, displayGl.UNSIGNED_BYTE, buf);
                return (x, y) => {
                    if (x >= 0 && y > 0 && x < canvas.width && y <= canvas.height) {
                        const idx = 4 * (x + (canvas.height - y - 1) * canvas.width);
                        return [ buf[idx + 0], buf[idx + 1], buf[idx + 2], buf[idx + 3] ];
                    }
                    return [ 0, 0, 0, 0 ];
                };
            })();
            output((x, y) => {
                const point = planeObj.localToWorld(new THREE.Vector3(x * scale, y * scale, 0.0)).add(hit)
                const ndc = point.project(camera);
                const pixelX = ((1.0 + ndc.x) * window.innerWidth * window.devicePixelRatio / 2.0) | 0;
                const pixelY = ((1.0 - ndc.y) * window.innerHeight * window.devicePixelRatio / 2.0) | 0;
                return display(pixelX, pixelY);
            });
        }
    })();
})();
