(function() {
    function stroke(path, offset) {
        const outerPath = OffsetUtils.offsetPath(path, offset, true);
        const innerPath = OffsetUtils.offsetPath(path, -offset, true);
        return [ outerPath, innerPath ];//OffsetUtils.joinOffsets(outerPath.clone(), innerPath.clone(), path, offset).unite();
    }
    function aspect() {
        return window.innerWidth / window.innerHeight;
    }
    const camera = new THREE.PerspectiveCamera(50, aspect(), 0.01, 100);
    camera.position.set(10, 5, 0);
    camera.lookAt(new THREE.Vector3(0, 0, 0));
    const arm = new THREE.Object3D();
    arm.rotateY(Math.PI / 4 + Math.PI);
    arm.add(camera);
    const scene = new THREE.Scene();
    scene.add(arm);
    scene.add(new THREE.GridHelper(10, 14, '#000000', '#636363'));
    const data = [
        [  1.33,   0.02 ],
        [  1.34,  -0.46 ],
        [  1.33,  -0.94 ],
        [  1.20,  -1.41 ],
        [  1.10,  -1.87 ],
        [  0.68,  -2.11 ],
        [  0.20,  -2.11 ],
        [ -0.27,  -2.01 ],
        [ -0.64,  -1.71 ],
        [ -0.72,  -1.23 ],
        [ -0.67,  -0.76 ],
        [ -0.71,  -0.28 ],
        [ -0.76,   0.20 ],
        [ -0.81,   0.68 ],
        [ -1.21,   0.94 ],
        [ -1.69,   0.99 ],
        [ -2.16,   0.89 ],
        [ -2.61,   0.73 ],
        [ -3.03,   0.49 ],
        [ -3.32,   0.10 ],
        [ -3.48,  -0.35 ],
        [ -3.53,  -0.83 ],
        [ -3.51,  -1.31 ],
        [ -3.46,  -1.78 ],
        [ -3.37,  -2.25 ],
        [ -3.12,  -2.66 ],
        [ -2.89,  -3.08 ],
        [ -2.47,  -3.32 ],
        [ -1.99,  -3.37 ],
        [ -1.51,  -3.41 ],
        [ -1.03,  -3.45 ],
        [ -0.55,  -3.44 ],
        [ -0.07,  -3.47 ],
        [  0.40,  -3.50 ],
        [  0.88,  -3.49 ],
        [  1.36,  -3.47 ],
        [  1.83,  -3.36 ],
        [  2.31,  -3.36 ],
        [  2.76,  -3.20 ],
        [  3.22,  -3.05 ],
        [  3.47,  -2.64 ],
        [  3.45,  -2.16 ],
        [  3.45,  -1.68 ],
        [  3.43,  -1.20 ],
        [  3.38,  -0.72 ],
        [  3.35,  -0.24 ],
        [  3.34,   0.24 ],
        [  3.35,   0.72 ],
        [  3.40,   1.19 ],
        [  3.52,   1.66 ],
        [  3.53,   2.14 ],
        [  3.45,   2.61 ],
        [  3.45,   3.09 ],
        [  3.18,   3.49 ],
        [  2.70,   3.50 ],
        [  2.22,   3.48 ],
        [  1.74,   3.46 ],
        [  1.37,   3.16 ],
        [  1.09,   2.77 ],
        [  1.06,   2.29 ],
        [  1.08,   1.81 ],
        [  1.20,   1.34 ],
        [  1.27,   0.87 ]
     ];
    const path = new THREE.Path(data.map(([x, y]) => new THREE.Vector2(x, y)));
    path.closePath();

    // Add Segments
   	const geom = new THREE.BufferGeometry().setFromPoints(path.getPoints().map(vec2 => new THREE.Vector3(vec2.x, 0.0, vec2.y)));
   	geom.computeBoundingBox();
   	//scene.add(new THREE.Line(geom, new THREE.LineBasicMaterial({ color: 0xffffff, depthTest: false, transparent: true, opacity: 0.75 })));

    // Simplify
    paper.setup();
    const pathCurve = new paper.Path({ segments: data, closed: true });
    pathCurve.simplify(0.0625);

    // Bezier
    function paperToThreePath(paperPath) {
        const threePath = new THREE.Path();
        threePath.moveTo(paperPath.firstCurve.point1.x, paperPath.firstCurve.point1.y);
        paperPath.curves.forEach((curve) => {
            const cp1 = curve.point1.add(curve.handle1);
            const cp2 = curve.point2.add(curve.handle2);
            threePath.bezierCurveTo(cp1.x, cp1.y, cp2.x, cp2.y, curve.point2.x, curve.point2.y);
        });
        threePath.closed = paperPath.closed;
        return threePath;
    }
    function paperToThreeCurve(paperPath) {
        const threeCurve = new THREE.CurvePath();
        paperPath.curves.forEach((curve) => {
            const cp1 = curve.point1.add(curve.handle1);
            const cp2 = curve.point2.add(curve.handle2);
            threeCurve.add(new THREE.CubicBezierCurve3(
                new THREE.Vector3(curve.point1.x, 0, curve.point1.y),
                new THREE.Vector3(cp1.x, 0, cp1.y),
                new THREE.Vector3(cp2.x, 0, cp2.y),
                new THREE.Vector3(curve.point2.x, 0, curve.point2.y)
            ));
        });
        return threeCurve;
    }
    const bezierPath = paperToThreeCurve(pathCurve);

    const roadHeight = 0.075;
    const roadWidth = 0.5;

    // Extrude Road Path
    const shape = new THREE.Shape();
    shape.moveTo(0, -roadWidth);
    shape.lineTo(-roadHeight, -roadWidth);
    shape.lineTo(-roadHeight, roadWidth);
    shape.lineTo(0, roadWidth);
    shape.lineTo(0, -roadWidth);
    const roadGeometry = new THREE.ExtrudeGeometry(shape, {
        steps: (bezierPath.getLength() * 4) | 0,
        bevelEnabled: false,
        extrudePath: bezierPath
    });
    // Extrude Road Up
    /*
   	const bezierGeom = new THREE.BufferGeometry().setFromPoints(bezierPath.getPoints().map(vec2 => new THREE.Vector3(vec2.x, 0.0, vec2.y)));
   	//scene.add(new THREE.Line(bezierGeom, new THREE.LineBasicMaterial({ color: 0xffff00, depthTest: false, transparent: true })));
    //const bezierMesh = new MeshLine();
    //bezierMesh.setGeometry(bezierGeom);
    //const surface = new THREE.Mesh(bezierMesh.geometry, new MeshLineMaterial({ color: 0x2c2f33, sizeAttenuation: true, lineWidth: 0.75, outline: true }));
    //scene.add(surface);
    const stroked = stroke(pathCurve, roadWidth);
    const shape = new THREE.Shape();
    shape.moveTo(stroked[0].firstCurve.point1.x, stroked[0].firstCurve.point1.y);
    stroked[0].curves.forEach((curve) => {
        const cp1 = curve.point1.add(curve.handle1);
        const cp2 = curve.point2.add(curve.handle2);
        shape.bezierCurveTo(cp1.x, cp1.y, cp2.x, cp2.y, curve.point2.x, curve.point2.y);
    });
    shape.closed = true;
    shape.holes.push(paperToThreePath(stroked[1]));
    //stroked.forEach(child => {
    //    const p = paperToThreePath(child);
    //    const g = new THREE.Geometry().setFromPoints(p.getPoints(50).map(vec2 => new THREE.Vector3(vec2.x, 0.0, vec2.y)));
    //    scene.add(new THREE.Line(g, new THREE.LineBasicMaterial({ color: 0x7f7f00, depthTest: false, transparent: true })));
    //});
    //const mm = new THREE.ShapeGeometry(shape);
    //mm.rotateX(0.5 * Math.PI);
    //scene.add(new THREE.Mesh(mm, new THREE.MeshBasicMaterial({ color: 0x2c2f33, side: THREE.BackSide })));
    const roadGeometry = new THREE.ExtrudeGeometry(shape, {
        bevelEnabled: false,
        depth: roadHeight
    });
    roadGeometry.rotateX(0.5 * Math.PI);
    roadGeometry.translate(0, roadHeight, 0);*/

    const tex = new THREE.TextureLoader().load('images/road_surface.png');
    tex.wrapS = THREE.RepeatWrapping;
    tex.wrapT = THREE.RepeatWrapping;
    tex.repeat.set(0.4, 0.4);
    const roadMesh = new THREE.Mesh(roadGeometry, new THREE.MeshStandardMaterial({
         color: 0xffffff,//color: 0x2c2f33,
         roughness : 0.7,
         //map: tex,
         wireframe: true
     }));
    roadMesh.receiveShadow = true;
    scene.add(roadMesh);

    const teapotSize = 0.175;
    const teapot = new THREE.Mesh(
        new THREE.TeapotBufferGeometry(teapotSize, 8),
        new THREE.MeshPhongMaterial({ side: THREE.DoubleSide })
    );
    teapot.castShadow = true;
    scene.add(teapot);
    // Lights
    scene.add(new THREE.AmbientLight(0xaaaaaa));
    const light = new THREE.DirectionalLight(0xccccff, 0.3, 100);
    light.position.set(30, 40, -60);
    light.castShadow = true;
    scene.add(light);

    const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
    renderer.setPixelRatio(window.devicePixelRatio);
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.setClearColor(0x000000, 0.0);
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    window.addEventListener('resize', function() {
            camera.aspect = aspect();
            camera.updateProjectionMatrix();
            renderer.setSize(window.innerWidth, window.innerHeight);
        });
    document.body.appendChild(renderer.domElement);
    const clock = new THREE.Clock();
    let upos = 0.0;
    (function animate() {
        const delta = clock.getDelta();
        requestAnimationFrame(animate);
        //arm.rotateY(0.33 * delta);
        const point = bezierPath.getPointAt(upos);
        teapot.position.set(point.x, roadHeight + teapotSize, point.z);
        teapot.lookAt(teapot.position.clone().add(bezierPath.getTangent(upos)));
        teapot.rotateOnAxis(teapot.up, -0.5 * Math.PI);
        upos = (upos + delta * 0.1) % 1.0;
        renderer.render(scene, camera);
    })();
})();
