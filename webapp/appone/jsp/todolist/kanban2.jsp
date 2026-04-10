<%@ page contentType="text/html; charset=utf-8" %>

<script src="/scheduler/appone/plugins/jquery/jquery-knob/jquery.knob.min.js"></script>
<script src="/scheduler/appone/plugins/dist/js/dashboard.js"></script>
<div class="container-fluid h-100">
	<!-- Main row -->
	<div class="row">
		<!-- Left col -->
		<section class="col-lg-3">
			<div class="card card-row card-secondary connectedSortable">
				<div class="card-header">
					<h3 class="card-title">아침</h3>
				</div>
					<div class="card card-info card-outline">
						<div class="card-header">
							<h5 class="card-title">Create Labels</h5>
							<div class="card-tools">
								<a href="#" class="btn btn-tool btn-link">#3</a> 
								<a href="#" class="btn btn-tool"> <i class="fas fa-pen"></i>
								</a>
							</div>
						</div>
						<div class="card-body p-2">
							<div class="custom-control custom-checkbox">
								<input class="custom-control-input" type="checkbox"
									id="customCheckbox1"> <label
									for="customCheckbox1" class="custom-control-label">Bug</label>
							</div>
							<div class="custom-control custom-checkbox">
								<input class="custom-control-input" type="checkbox"
									id="customCheckbox2" > <label
									for="customCheckbox2" class="custom-control-label">Feature</label>
							</div>
							<div class="custom-control custom-checkbox">
								<input class="custom-control-input" type="checkbox"
									id="customCheckbox3" > <label
									for="customCheckbox3" class="custom-control-label">Enhancement</label>
							</div>
							<div class="custom-control custom-checkbox">
								<input class="custom-control-input" type="checkbox"
									id="customCheckbox4" > <label
									for="customCheckbox4" class="custom-control-label">Documentation</label>
							</div>
							<div class="custom-control custom-checkbox">
								<input class="custom-control-input" type="checkbox"
									id="customCheckbox5" > <label
									for="customCheckbox5" class="custom-control-label">Examples</label>
							</div>
						</div>
					</div>
			</div>
		</section>
		
		<section class="col-lg-3">
			<div class="card card-row card-primary connectedSortable">
				<div class="card-header">
					<h3 class="card-title">점심</h3>
				</div>
				<div class="card card-warning card-outline">
                    <a class="d-block w-100 collapsed" data-toggle="collapse" href="#collapseFour" aria-expanded="false">
                        <div class="card-header">
                            <h4 class="card-title w-100">
                                4. Donec pede justo
                            </h4>
                        </div>
                    </a>
                    <div id="collapseFour" class="collapse" data-parent="#accordion" style="">
                        <div class="card-body p-2">
                            Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu.
                        </div>
                    </div>
                </div>
                
				<div class="card card-primary card-outline">
					<div class="card-header">
						<h5 class="card-title">Create first milestone</h5>
						<div class="card-tools">
							<a href="#" class="btn btn-tool btn-link">#5</a> <a href="#"
								class="btn btn-tool"> <i class="fas fa-pen"></i>
							</a>
						</div>
					</div>
				</div>
			</div>
		</section>
		<section class="col-lg-3">
			<div class="card card-row card-default connectedSortable">
				<div class="card-header bg-info">
					<h3 class="card-title">저녁</h3>
				</div>
					<div class="card card-light card-outline">
						<div class="card-header">
							<h5 class="card-title">Update Readme</h5>
							<div class="card-tools">								
								<a href="#" class="btn btn-tool"> <i class="fas fa-pen"></i></a>
							</div>
						</div>
						<div class="card-body p-2">
							<p>Lorem ipsum dolor sit amet, consectetuer adipiscing elit.
								Aenean commodo ligula eget dolor. Aenean massa. Cum sociis
								natoque penatibus et magnis dis parturient montes, nascetur
								ridiculus mus.</p>
						</div>
					</div>
			</div>
		</section>
		
		<section class="col-lg-3">
			<div class="card card-row card-success connectedSortable">
				<div class="card-header">
					<h3 class="card-title">성공여부</h3>
				</div>
				<div class="card-body p-2">
					<div class="card card-primary card-outline">
						<div class="card-header">
							<h5 class="card-title">Create repo</h5>
							<div class="card-tools">
								<a href="#" class="btn btn-tool btn-link">#1</a> <a href="#"
									class="btn btn-tool"> <i class="fas fa-pen"></i>
								</a>
							</div>
						</div>
					</div>
				</div>
			</div>
		</section>
	</div>
</div>